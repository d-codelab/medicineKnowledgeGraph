import { request } from '../utils/request'
import { requestKG } from '../utils/requestKG'

/**
 * 发送post请求
 * @param url
 * @param data
 * @returns {AxiosPromise}
 */

export function login (data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

export function logout (data) {
  return request({
    url: '/api/user/logout',
    method: 'post',
    data
  })
}

export function register (data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
}

export function getCode (data) {
  return request({
    url: '/api/user/getCode',
    method: 'get',
    params:{
      email:data
    }
  })
}

export function getEmail (data) {
  return request({
    url: '/api/user/getEmail',
    method: 'get',
    params:{
      username:data
    }
  })
}

export function getGraph (data) {
  return requestKG({
    url: '/getDiseaseNodes',
    method: 'get',
    data
  })
}

export function resetPassword (data) {
  return request({
    url: '/api/user/forgetPassword',
    method: 'post',
    data
  })
}

export function fetchArticles (query) {
  return request({
    url: '/api/articles/list/page',
    method: 'post',
    data: query
  })
}

export function fetchArticleID (query) {
  return request({
    url: '/api/articles/get',
    method: 'get',
    params: query
  })
}

export function searchNode(data) {
  return requestKG({
    url: '/manage/kg/matchNodeName',
    method:'get',
    params: {
      name:data
    }
  })
}

export function queNode(data) {
  return requestKG({
    url: '/bot',
    method:'get',
    params: {
      spoken:data
    }
  })
}

export function fetchArticleTag () {
  return request({
    url: '/api/category/list',
    method: 'get',
  })
}
