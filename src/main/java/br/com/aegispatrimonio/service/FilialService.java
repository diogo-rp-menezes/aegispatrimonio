package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.mapper.FilialMapper;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.TipoFilial;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilialService {

    private static final Logger logger = LoggerFactory.getLogger(FilialService.class);

    private final FilialRepository filialRepository;
    private final DepartamentoRepository departamentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final FilialMapper filialMapper;
    private final CurrentUserProvider currentUserProvider; // Injetando CurrentUserProvider

    public FilialService(FilialRepository filialRepository, DepartamentoRepository departamentoRepository,
            FuncionarioRepository funcionarioRepository, FilialMapper filialMapper,
            CurrentUserProvider currentUserProvider) {
        this.filialRepository = filialRepository;
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.filialMapper = filialMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<FilialDTO> listarTodos() {
        return filialRepository.findAll()
                .stream()
                .map(filialMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<FilialDTO> buscarPorId(Long id) {
        var opt = filialRepository.findById(id);
        logger.debug("FilialService.buscarPorId: id={} present={}", id, opt.isPresent());
        return opt.map(filialMapper::toDTO);
    }

    @Transactional
    public FilialDTO criar(FilialCreateDTO filialCreateDTO) {
        validarUnicidade(filialCreateDTO.cnpj(), filialCreateDTO.codigo(), null);
        validarTipoMatriz(filialCreateDTO.tipo(), null);

        Filial filial = filialMapper.toEntity(filialCreateDTO);
        Filial filialSalva = filialRepository.save(filial);

        Usuario usuarioLogado = currentUserProvider.getCurrentUsuario();
        logger.info("AUDIT: Usuário {} criou a filial com ID {} e nome {}.", usuarioLogado.getEmail(),
                filialSalva.getId(), filialSalva.getNome());

        return filialMapper.toDTO(filialSalva);
    }

    @Transactional
    public Optional<FilialDTO> atualizar(Long id, FilialUpdateDTO filialUpdateDTO) {
        Optional<Filial> optionalFilial = filialRepository.findById(id);

        if (optionalFilial.isEmpty()) {
            return Optional.empty();
        }

        Filial filial = optionalFilial.get();

        validarUnicidade(filialUpdateDTO.cnpj(), filialUpdateDTO.codigo(), id);
        validarTipoMatriz(filialUpdateDTO.tipo(), id);

        filial.setNome(filialUpdateDTO.nome());
        filial.setCodigo(filialUpdateDTO.codigo());
        filial.setTipo(filialUpdateDTO.tipo());
        filial.setCnpj(filialUpdateDTO.cnpj());
        filial.setEndereco(filialUpdateDTO.endereco());
        filial.setStatus(filialUpdateDTO.status());

        Filial filialAtualizada = filialRepository.save(filial);

        Usuario usuarioLogado = currentUserProvider.getCurrentUsuario();
        logger.info("AUDIT: Usuário {} atualizou a filial com ID {} e nome {}.", usuarioLogado.getEmail(),
                filialAtualizada.getId(), filialAtualizada.getNome());

        return Optional.of(filialMapper.toDTO(filialAtualizada));
    }

    @Transactional
    public void deletar(Long id) {
        Filial filial = filialRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filial não encontrada com ID: " + id));

        if (departamentoRepository.existsByFilialId(id)) {
            throw new IllegalStateException(
                    "Não é possível deletar a filial, pois existem departamentos associados a ela.");
        }

        if (funcionarioRepository.existsByFiliais_Id(id)) {
            throw new IllegalStateException(
                    "Não é possível deletar a filial, pois existem funcionários associados a ela.");
        }

        filialRepository.delete(filial);

        Usuario usuarioLogado = currentUserProvider.getCurrentUsuario();
        logger.info("AUDIT: Usuário {} deletou a filial com ID {} e nome {}.", usuarioLogado.getEmail(), id,
                filial.getNome());
    }

    private void validarUnicidade(String cnpj, String codigo, Long id) {
        Optional<Filial> filialPorCnpj = filialRepository.findByCnpj(cnpj);
        if (filialPorCnpj.isPresent() && !filialPorCnpj.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe uma filial cadastrada com o CNPJ informado.");
        }

        Optional<Filial> filialPorCodigo = filialRepository.findByCodigo(codigo);
        if (filialPorCodigo.isPresent() && !filialPorCodigo.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe uma filial cadastrada com o código informado.");
        }
    }

    private void validarTipoMatriz(TipoFilial tipo, Long id) {
        if (tipo == TipoFilial.MATRIZ) {
            Optional<Filial> matrizExistente = filialRepository.findByTipo(TipoFilial.MATRIZ);
            if (matrizExistente.isPresent() && !matrizExistente.get().getId().equals(id)) {
                throw new IllegalArgumentException("Já existe uma filial cadastrada como MATRIZ.");
            }
        }
    }
}
