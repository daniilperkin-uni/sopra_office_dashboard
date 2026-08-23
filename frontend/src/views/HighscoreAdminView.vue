<template>
  <div class="highscore-admin-view">
    <HighscoreAdminList />
    <EloRankingsTable />
  </div>
</template>

<script setup>
/**
 * View component for highscore management.
 *
 * Wraps the match administration list and the ELO rankings table.
 */
import HighscoreAdminList from '@/features/highscore/admin/HighscoreAdminList.vue'
import EloRankingsTable from '@/features/highscore/components/EloRankingsTable.vue'
</script>

<style scoped>
/* Add specific styles for HighscoreAdminView if needed */
</style>
