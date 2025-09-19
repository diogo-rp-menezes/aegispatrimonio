<template>
  <nav class="navbar navbar-expand navbar-light bg-white shadow-sm px-3">
    <div class="container-fluid">
      <form class="d-flex me-auto">
        <input class="form-control form-control-sm me-2" type="search" placeholder="Pesquisar..." aria-label="Search" />
        <button class="btn btn-outline-primary btn-sm" type="submit">
          <i class="bi bi-search"></i>
        </button>
      </form>

      <ul class="navbar-nav ms-auto align-items-center">
        <li class="nav-item me-3">
          <a href="#" class="nav-link position-relative">
            <i class="bi bi-bell fs-5"></i>
            <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
              3
              <span class="visually-hidden">notificações</span>
            </span>
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link">
            <i class="bi bi-person-circle fs-5"></i>
          </a>
        </li>
      </ul>
    </div>
  </nav>
</template>
