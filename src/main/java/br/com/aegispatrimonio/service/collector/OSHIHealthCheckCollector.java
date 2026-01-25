package br.com.aegispatrimonio.service.collector;

import br.com.aegispatrimonio.model.HealthCheckHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OSHIHealthCheckCollector {

    private final ObjectMapper objectMapper;
    private final SystemInfo systemInfo;

    public OSHIHealthCheckCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.systemInfo = new SystemInfo();
    }

    public HealthCheckHistory collect() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        HealthCheckHistory history = new HealthCheckHistory();

        // Hostname
        try {
            history.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            history.setHost("unknown");
        }

        // CPU Usage
        CentralProcessor processor = hal.getProcessor();
        double cpuLoad = processor.getSystemCpuLoad(1000);
        if (Double.isNaN(cpuLoad)) {
            cpuLoad = 0.0;
        }
        history.setCpuUsage(BigDecimal.valueOf(cpuLoad).setScale(4, RoundingMode.HALF_UP));

        // Memory
        GlobalMemory memory = hal.getMemory();
        double memFreePercent = (double) memory.getAvailable() / memory.getTotal();
        history.setMemFreePercent(BigDecimal.valueOf(memFreePercent).setScale(4, RoundingMode.HALF_UP));

        // Disks (File Systems)
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        List<Map<String, Object>> fsList = new ArrayList<>();
        for (OSFileStore fs : fileStores) {
            Map<String, Object> map = new HashMap<>();
            map.put("mount", fs.getMount());
            map.put("total", fs.getTotalSpace());
            map.put("free", fs.getUsableSpace());
            if (fs.getTotalSpace() > 0) {
                 double freePct = (double) fs.getUsableSpace() / fs.getTotalSpace();
                 map.put("freePercent", BigDecimal.valueOf(freePct).setScale(4, RoundingMode.HALF_UP));
            }
            fsList.add(map);
        }
        try {
            history.setDisks(objectMapper.writeValueAsString(fsList));
        } catch (JsonProcessingException e) {
            history.setDisks("[]");
        }

        // Network
        List<NetworkIF> networkIFs = hal.getNetworkIFs();
        List<Map<String, Object>> netList = new ArrayList<>();
        for (NetworkIF net : networkIFs) {
            net.updateAttributes();
            Map<String, Object> map = new HashMap<>();
            map.put("interfaceName", net.getName());
            map.put("bytesTx", net.getBytesSent());
            map.put("bytesRx", net.getBytesRecv());
            netList.add(map);
        }
        try {
            history.setNets(objectMapper.writeValueAsString(netList));
        } catch (JsonProcessingException e) {
            history.setNets("[]");
        }

        return history;
    }
}
