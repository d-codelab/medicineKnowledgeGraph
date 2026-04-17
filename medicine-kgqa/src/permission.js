// import store from "./store";
// import router from "./router";

// // 设置路由守卫，在进页面之前，判断有token，才进入页面，否则返回登录页面
// const whiteList = ["Login", "Register", "Reset"]; // no redirect whitelist
// router.beforeEach((to, from, next) => {
//   if (whiteList.includes(to.name)) {
//     next();
//   } else {
//     let token = store.state.token;
//     if (token === "" || token === undefined) {
//       next("/login");
//     } else {
//       next("/");
//     }
//   }
// });
