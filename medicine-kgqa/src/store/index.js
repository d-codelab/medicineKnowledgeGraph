import { login } from '../api/api'
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: ''
  },
  mutations: {
    setToken(state, token) {
      state.token = token
    }
  },
  actions: {
    Login({ commit }, values) {
      return new Promise((resolve, reject) => {
        login(values).then(res => {
          console.log('login res-->',res)
          commit('setToken', res.data.token)
          sessionStorage.setItem('token', res.data.token)
          resolve()
        }).catch(err => {
          reject(err)
        })
      })
    }
  }
})
