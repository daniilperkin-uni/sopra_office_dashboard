import { createRouter, createWebHistory } from 'vue-router'
import DisplayView from '../views/DisplayView.vue'
import AdminView from '../views/AdminView.vue'
import HighscoreAdminView from '../views/HighscoreAdminView.vue'
import ParkingAdminSection from '../views/ParkingAdminSection.vue'
import LunchAdminSection from '../views/LunchAdminSection.vue'
import DisplayConfigAdmin from '@/features/dashboard/admin/DisplayConfigAdmin.vue'

/**
 * Route definitions for the application.
 * Includes the main display view and the administrative sub-sections.
 */
const routes = [
  { path: '/', redirect: '/display' },
  { path: '/display', component: DisplayView },
  {
    path: '/admin',
    component: AdminView,
    children: [
      { path: '', redirect: 'parking' },
      { path: 'parking', component: ParkingAdminSection },
      { path: 'highscores', component: HighscoreAdminView },
      { path: 'lunch', component: LunchAdminSection },
      { path: 'config', component: DisplayConfigAdmin },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router