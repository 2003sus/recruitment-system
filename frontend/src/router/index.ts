import { createRouter, createWebHistory } from 'vue-router'
import RecruitmentList from '../views/RecruitmentList.vue'
import Login from '../views/Login.vue'
import StatsDashboard from '../views/StatsDashboard.vue'
import InterviewCalendar from '../views/InterviewCalendar.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/', component: RecruitmentList },
    { path: '/stats', component: StatsDashboard },
    { path: '/interview', component: InterviewCalendar }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
})

export default router