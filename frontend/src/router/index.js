import { createRouter, createWebHistory } from 'vue-router'
import DisplayView from '../views/DisplayView.vue'
import AdminView from '../views/AdminView.vue'
import HighscoreAdminView from '../views/HighscoreAdminView.vue'
import ParkingAdminSection from '../views/ParkingAdminSection.vue'
import LunchAdminSection from '../views/LunchAdminSection.vue'
import DisplayConfigAdmin from '@/features/dashboard/admin/DisplayConfigAdmin.vue'
import { authService } from '@/services/authService'

/**
 * Route definitions for the application.
 * Includes the main display view, login, and the administrative sub-sections.
 */
const routes = [
  { path: '/', redirect: '/display' },
  { path: '/display', component: DisplayView },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
  },
  {
    path: '/admin',
    component: AdminView,
    meta: { requiresAuth: true },
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

router.beforeEach(async (to) => {
  if (to.meta.requiresAuth) {
    if (!authService.authChecked.value) {
      await authService.checkAuth()
    }
    if (!authService.isAuthenticated.value) {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
