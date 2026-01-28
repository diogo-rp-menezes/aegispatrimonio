<template>
  <div class="card border-0 shadow-sm h-100">
    <div class="card-body">
      <h5 class="fw-bold mb-4">Distribuição de Saúde (Preditiva)</h5>
      <div class="chart-container" style="position: relative; height: 300px; width: 100%; display: flex; justify-content: center;">
        <Doughnut :data="chartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'vue-chartjs';

ChartJS.register(ArcElement, Tooltip, Legend);

const props = defineProps({
  predictiveStats: {
    type: Array,
    required: true,
    default: () => []
  }
});

const chartData = computed(() => {
  // Mapping bootstrap colors to hex
  const colorMap = {
    'danger': '#dc3545',
    'warning': '#ffc107',
    'success': '#198754',
    'primary': '#0d6efd',
    'secondary': '#6c757d',
    'info': '#0dcaf0'
  };

  const labels = props.predictiveStats.map(s => s.title);
  const data = props.predictiveStats.map(s => s.value);
  const backgroundColor = props.predictiveStats.map(s => colorMap[s.color] || '#ccc');

  return {
    labels: labels,
    datasets: [
      {
        backgroundColor: backgroundColor,
        data: data
      }
    ]
  };
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        usePointStyle: true,
        padding: 20
      }
    }
  }
};
</script>
