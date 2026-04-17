<template>
  <div v-loading="loading" element-loading-text="登录中..." element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.6)" class="login-container">

    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" auto-complete="on" label-position="left">
      <!-- 头像区域 -->
      <div v-if="TxStatus" class="avatar-box">
<!--        <img src="../assets/home1.jpg">-->
        <img src="../../public/logo.png">
      </div>

      <div class="title-container">
        <h3 class="title">智医图说</h3>
      </div>

      <el-form-item prop="username">
        <span class="icon-container">
          <i class="el-icon-user-solid"></i>
        </span>
        <el-input ref="username" v-model="loginForm.username" placeholder="请输入用户名" name="username" type="text" tabindex="1" auto-complete="on" />
      </el-form-item>

      <el-form-item prop="userPassword">
        <span class="icon-container">
          <i class="el-icon-lock"></i>
        </span>
        <el-input ref="password" v-model="loginForm.userPassword" type="password" placeholder="请输入密码" name="userPassword" tabindex="2" auto-complete="on" @keyup.enter.native="handleLogin" />
      </el-form-item>

      <div>
        <el-button type="primary" style="width:100%;margin-bottom:20px;" @click.native.prevent="handleLogin">登录</el-button>
      </div>
      <div class="tips">
        <span style="margin-right:20px;color: #fff">如果您还没有账号请先 <span style="color:#33CCCC;cursor:pointer" @click="register">注册</span></span>
<!--        <span style="color:#409EFF;cursor:pointer" @click="toHome">游客登录</span>-->
        <span style="color:#33CCCC;cursor:pointer" @click="reset">忘记密码</span>
      </div>

    </el-form>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {

      // 检查用户名是否为空
      if (!value || value.trim() === '') {
        callback(new Error('用户名不能为空'));
      } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        // 使用正则表达式检查用户名是否只包含字母、数字和下划线
        callback(new Error('用户名只能包含字母、数字和下划线'));
      } else {
        // 用户名符合要求，回调无错误
        callback();
      }
    }
    const validatePassword = (rule, value, callback) => {
      const strongRegex = /^(?=.*\d)(?=.*[a-zA-Z]).{8,16}$/
      if (!strongRegex.test(value)) {
        callback(new Error('密码最少8个字符，最多16个字符，包含数字和字母！'))
      } else {
        callback()
      }
    }
    return {
      // 头像状态
      TxStatus: true,
      loginForm: {
        username: '',
        userPassword: ''
      },
      // 登录规则
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        userPassword: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  methods: {
    toHome(){
      this.$router.push('/home')
    },
    // 登录业务
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        // 如果符合验证规则
        if (valid) {
          this.loading = true
          this.$store.dispatch('Login', this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || '/home' })
            this.loading = false
          }).finally(() => {
            this.loading = false
            // this.$router.push({ path: this.redirect || '/home' })
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    // 注册业务
    register() {
      this.$router.push('/register')
    },
    // 重置密码
    reset() {
      this.$router.push('/reset')
    },
  }
}
</script>

<style lang="scss">
$bg: #283443;
$light_gray: #fff;
$cursor: #fff;
@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}
.login-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.login-container {
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  margin:0px;
  //background-color: #182245;
  background: url(../assets/background2.gif);
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  background-attachment:fixed;
  // 头像
  .avatar-box {
    margin: 0 auto;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    border: 1px solid #409eff;
    box-shadow: 0 0 10px #409eff;
    position: relative;
    bottom: 20px;
    img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }
  }

  .login-form {
    position: relative;
    width: 520px;
    max-width: 100%;
    padding: 60px 35px 0;
    margin: 0 auto;
    overflow: hidden;

  }

  .tips {
    font-size: 20px;
    text-align: center;
    color: #000;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
  }

  .icon-container {
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
    font-size: 20px;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 30px;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: 500;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }
}
</style>
