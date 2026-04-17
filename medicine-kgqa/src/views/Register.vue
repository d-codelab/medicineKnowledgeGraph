<template>
  <div v-loading="loading" element-loading-text="登录中..." element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.6)" class="login-container">

    <el-form ref="registerForm" :model="registerForm" :rules="loginRules" class="login-form" auto-complete="on" label-width="80px" label-position="left">
      <!-- 头像区域 -->
      <div v-if="TxStatus" class="avatar-box">
        <!--        <img src="../assets/home1.jpg">-->
        <img src="../../public/logo.png">
      </div>

      <div class="title-container">
        <h3 class="title">用户注册</h3>
      </div>

      <el-form-item label="用户名"  prop="username">
        <el-input ref="username" v-model="registerForm.username" placeholder="请输入用户名" name="username" type="text" tabindex="1" auto-complete="on" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input ref="email" v-model="registerForm.email" placeholder="请输入正确邮箱地址，忘记密码时可用邮箱重设密码" name="email" type="text" tabindex="1" auto-complete="on" />
      </el-form-item>

      <el-form-item label="验证码" prop="code">
          <div style="width:428px;display: flex;align-items: center;justify-content: space-between;">
            <el-input v-model="registerForm.code" placeholder="请输入验证码"></el-input>
            <el-button type="primary" slot="append" @click="sendCode">获取验证码</el-button>
          </div>
<!--        <el-col :span="10">-->
<!--          <el-input v-model="registerForm.code" placeholder="请输入验证码"></el-input>-->
<!--        </el-col>-->
<!--        <el-col :span="4">-->
<!--          <el-button type="primary" @click="handleSubmit('captcha')">确认验证码</el-button>-->
<!--        </el-col>-->
      </el-form-item>

      <el-form-item label="密码"  prop="password">
        <el-input ref="password" v-model="registerForm.password" type="password" placeholder="请输入密码" name="password" tabindex="2" auto-complete="on"/>
      </el-form-item>

      <el-form-item label="确认密码" prop="checkPassword">
        <el-input ref="checkPassword" v-model="registerForm.checkPassword" type="password" placeholder="请再次输入密码" name="checkPassword" tabindex="2" auto-complete="on" @keyup.enter.native="handleRegister" />
      </el-form-item>

      <div>
        <el-button type="primary" style="width:100%;margin-bottom:20px;" @click.native.prevent="handleRegister">注册</el-button>
      </div>
      <div class="tips">
        <span style="margin-right:20px;color: #fff">返回到 <span style="color:#33CCCC;cursor:pointer" @click="login">登录</span></span>
      </div>

    </el-form>
  </div>
</template>

<script>

import { register, getCode } from "../api/api";
import {Message} from "element-ui";

export default {
  name: 'Register',
  data() {
    const validateUsername = (rule, value, callback) => {
      // 检查用户名是否为空
      if (!value || value.trim() === '') {
        callback(new Error('用户名不能为空'));
      } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        // 使用正则表达式检查用户名是否只包含字母、数字和下划线
        callback(new Error('用户名只能包含字母、数字和下划线'));
      } else if (value.length <= 4) {
        // 检查用户名长度是否大于4
        callback(new Error('用户名长度必须大于4'));
      } else {
        // 用户名符合要求，回调无错误
        callback();
      }
    };

    const validatePassword = (rule, value, callback) => {
        const strongRegex = /^(?=.*\d)(?=.*[a-zA-Z]).{8,16}$/
        if (!strongRegex.test(value)) {
          callback(new Error('密码最少8个字符，最多16个字符，必须包含数字和字母！'))
        } else {
          callback()
        }
    }

    const validateEmail = (rule, value, callback) => {
      // 使用正则表达式验证邮箱格式
      const emailRegex = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/;

      if (!emailRegex.test(value)) {
        // 如果邮箱格式不正确，则调用callback函数并传入一个错误对象
        callback(new Error('请输入正确的邮箱地址！'));
      } else {
        // 如果邮箱格式正确，则调用callback函数不传入任何参数
        callback();
      }
    };
    const validateConfirm = (rule, value, callback) => {
      const password = this.registerForm.password; // 获取password的值
      // 比较checkPassword和password是否相同
      if (value !== password) {
        callback(new Error('两次输入的密码不一致！'));
      } else {
        callback(); // 密码一致，校验通过
      }
    };
    return {
      // 头像状态
      TxStatus: true,
      registerForm: {
        username: '',
        password: '',
        checkPassword:'',
        email: '',
        code: '',
        role:'user'
      },
      // 登录规则
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        checkPassword: [{ required: true, trigger: 'blur', validator: validateConfirm }],
        email: [{ required: true, trigger: 'blur', validator: validateEmail }],
        code: [{ required: true, trigger: 'blur',message: '请输入验证码！',}]
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
    //获取验证码
    sendCode(){
      console.log('email',this.registerForm.email)
      getCode(this.registerForm.email).then(res => {
        console.log('getCode res-->',res)
        Message({
          message: '请前往邮箱查看！',
          type: 'success'
        });
      })
    },
    // 注册业务
    handleRegister() {
      console.log('this.registerForm',this.registerForm)
      this.$refs.registerForm.validate((valid) => {
        // 如果符合验证规则
        if (valid) {
          register(this.registerForm).then(res => {
            console.log('register res-->',res)
            this.$router.push('/login')
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    // 注册业务
    login() {
      this.$router.go(-1)
    }
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
::v-deep .el-form-item__content{
  display: flex;
  align-items: center;
  padding-left: 8px;
}
::v-deep .el-button{
  width: 120px;
  text-align: center;
}
::v-deep .el-form-item__label{
  margin-left: 10px;
  color: #fff;
}
</style>
