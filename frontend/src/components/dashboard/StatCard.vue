<template>
  <div class="stat-card" :class="cardClass">
    <div class="stat-header">
      <div>
        <div class="stat-label">{{ title }}</div>
        <div class="stat-value">{{ value }}</div>
      </div>
      <div class="stat-icon" :class="iconClass">
        <i :class="icon"></i>
      </div>
    </div>
    <div class="stat-change" :class="trendClass">
      <i :class="trendIcon"></i> {{ trendValue }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  icon: {
    type: String,
    required: true
  },
  trend: {
    type: String,
    default: 'neutral',
    validator: (value: string) => ['positive', 'negative', 'neutral'].includes(value)
  },
  trendValue: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: 'primary',
    validator: (value: string) => ['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'accent'].includes(value)
  }
})

const trendClass = computed(() => `stat-change-${props.trend}`)
const trendIcon = computed(() => 
  props.trend === 'positive' ? 'bi bi-arrow-up' : 
  props.trend === 'negative' ? 'bi bi-arrow-down' : 
  'bi bi-dash'
)

const iconClass = computed(() => `stat-icon-${props.color}`)
const cardClass = computed(() => `stat-card-${props.color}`)
</script>

<style scoped>
.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  border: 1px solid rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--aegis-accent), var(--aegis-secondary));
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.12);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-icon-primary {
  background: linear-gradient(135deg, var(--aegis-primary), var(--aegis-secondary));
}

.stat-icon-secondary {
  background: linear-gradient(135deg, var(--aegis-secondary), #4dabf7);
}

.stat-icon-success {
  background: linear-gradient(135deg, var(--aegis-success), #51cf66);
}

.stat-icon-danger {
  background: linear-gradient(135deg, var(--aegis-danger), #ff6b6b);
}

.stat-icon-warning {
  background: linear-gradient(135deg, var(--aegis-warning), #ffd43b);
}

.stat-icon-info {
  background: linear-gradient(135deg, var(--aegis-info), #339af0);
}

.stat-icon-accent {
  background: linear-gradient(135deg, var(--aegis-accent), #ffa726);
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--aegis-primary);
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.85rem;
  font-weight: 600;
}

.stat-change-positive {
  color: var(--aegis-success);
}

.stat-change-negative {
  color: var(--aegis-danger);
}

.stat-change-neutral {
  color: #6c757d;
}
</style>