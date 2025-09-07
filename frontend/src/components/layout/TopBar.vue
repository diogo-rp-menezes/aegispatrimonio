<template>
  <div class="top-bar">
    <button class="btn btn-outline-secondary d-lg-none" id="sidebarToggle">
      <i class="bi bi-list"></i>
    </button>
    
    <div class="search-bar">
      <i class="bi bi-search search-icon"></i>
      <input type="text" class="search-input" placeholder="Pesquisar patrimônio...">
    </div>
    
    <div class="user-profile">
      <div class="user-avatar">JS</div>
      <div class="user-info">
        <h6>John Smith</h6>
        <small>Administrador</small>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  const sidebarToggle = document.getElementById('sidebarToggle')
  const sidebar = document.querySelector('.sidebar')
  
  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener('click', () => {
      sidebar.classList.toggle('show')
    })
  }
})
</script>

<style scoped>
.top-bar {
  background: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 999;
}

.search-bar {
  position: relative;
  flex: 1;
  max-width: 400px;
  margin: 0 2rem;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 2px solid #e9ecef;
  border-radius: 50px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: var(--aegis-light);
}

.search-input:focus {
  outline: none;
  border-color: var(--aegis-accent);
  box-shadow: 0 0 0 3px rgba(245, 124, 0, 0.1);
  background: white;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.user-profile:hover {
  background: var(--aegis-light);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--aegis-accent), #ff9800);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.user-info h6 {
  margin: 0;
  font-size: 0.9rem;
}

.user-info small {
  color: #6c757d;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .top-bar {
    padding: 1rem;
  }
  
  .search-bar {
    margin: 0 1rem;
    max-width: 300px;
  }
}
</style>