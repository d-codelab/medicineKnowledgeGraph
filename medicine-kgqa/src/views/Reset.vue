<template>
  <div class="login-container">
    <!-- 头像区域 -->
    <div class="avatar-box">
      <!--        <img src="../assets/home1.jpg">-->
      <img src="../../public/logo.png">
    </div>
    <div class="password-reset">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px" label-position="left">
        <div class="title-container">
          <h3 class="title">重置密码</h3>
        </div>
        <el-form-item label="用户名" prop="username">
          <el-col :span="17">

            <el-input v-model="form.username" placeholder="请输入您的用户名"></el-input>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="getmail">获取注册邮箱</el-button>
          </el-col>
        </el-form-item>
        <el-form-item v-if="showCaptcha" label="邮箱" prop="email">
          <el-col :span="17">
            <el-input v-model="form.email" placeholder="请输入您注册时使用的邮箱"></el-input>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="sendCode">发送验证码</el-button>
          </el-col>
        </el-form-item>

        <el-form-item v-if="showCaptcha" label="验证码" prop="captcha">
            <el-input v-model="form.code" placeholder="请输入验证码"></el-input>
        </el-form-item>

        <el-form-item v-if="showCaptcha" label="新密码" prop="newPassword">
            <el-input type="password" v-model="form.newPassword" placeholder="请输入新密码"></el-input>
        </el-form-item>

        <el-form-item v-if="showCaptcha" label="确认密码" prop="checkPassword">
          <el-col :span="17">
            <el-input type="password" v-model="form.checkPassword" placeholder="请再次输入新密码"></el-input>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSubmit">确认新密码</el-button>
          </el-col>
        </el-form-item>
  <!--      <el-form-item v-if="showCaptcha" label="验证码" prop="captcha">-->
  <!--        <el-input v-model="form.captcha"></el-input>-->
  <!--      </el-form-item>-->
  <!--      <div v-if="showCaptcha">-->
  <!--        <el-button type="primary" @click="handleSubmit('captcha')">验证验证码</el-button>-->
  <!--      </div>-->
  <!--      <el-form-item v-if="showNewPassword" label="新密码" prop="newPassword">-->
  <!--        <el-input type="password" v-model="form.newPassword"></el-input>-->
  <!--      </el-form-item>-->
  <!--      <div v-if="showNewPassword">-->
  <!--        <el-button type="primary" @click="handleSubmit('newPassword')">重置密码</el-button>-->
  <!--      </div>-->
        <div class="tips">
          <span style="margin-right:20px;color: #fff">返回到 <span style="color:#33CCCC;cursor:pointer" @click="login">登录</span></span>
        </div>
      </el-form>
    </div>
    </div>
</template>

<script>
import { getCode, getEmail, resetPassword } from "../api/api";

export default {
  name: 'Reset',
  data() {
    const validateCaptcha = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入验证码'));
      } else {
        // 这里应该是向后端发送请求，验证验证码是否正确且未过期
        // 这里只是示例，直接假定验证通过
        callback();
      }
    };

    const validateNewPassword = (rule, value, callback) => {
      const strongRegex = /^(?=.*\d)(?=.*[a-zA-Z]).{8,16}$/
      if (!strongRegex.test(value)) {
        callback(new Error('密码最少8个字符，最多16个字符，必须包含数字和字母！'))
      } else {
        callback()
      }
    };

    return {
      form: {
        username:'',
        code: '',
        email:'',
        newPassword: '',
        checkPassword:''
      },
      rules: {
        username: [{ required: true, trigger: 'blur',message: '请输入您的用户名！',}],
        captcha: [{ validator: validateCaptcha, trigger: 'blur' }],
        newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
      },
      showCaptcha: false
    };
  },
  methods: {
    // back to login
    login() {
      this.$router.go(-1)
    },
    //获取验证码
    sendCode(){
      console.log('email',this.form.email)
      getCode(this.form.email).then(res => {
        console.log('getCode res-->',res)
      })
    },
    getmail(){
      getEmail(this.form.username).then(res => {
        console.log('email res-->',res)
        this.form.email = res.data
        this.showCaptcha = true
      })
    },
    handleSubmit() {
      console.log('form-->')
      console.log(this.form)
      this.$refs.form.validate((valid) => {
        if (valid) {
          // 重置密码
          resetPassword(this.form).then(res => {
            console.log('reset res-->',res)
            this.$message({message:'密码重置成功，请重新登录',type:'success'});
            this.$router.push('/login')
          })
        } else {
          console.log('error submit!!');
          this.$message({message:'密码重置失败',type:'warning'});
          return false;
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.title-container {
  position: relative;

.title {
  font-size: 30px;
  color: #eee;
  margin: 0px auto 40px auto;
  text-align: center;
  font-weight: 500;
}
}
.password-reset {
  width: 520px;
  margin: 20px auto;
}
.tips {
  font-size: 20px;
  text-align: center;
  color: #000;
  margin-bottom: 10px;
  display: flex;
}
.login-container {
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  margin: 0px;
  background: url(../assets/background2.gif);
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  background-attachment: fixed;
  // 头像
  .avatar-box {
    margin: 0 auto;
    margin-top: 60px;
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
}
::v-deep .el-form-item__content{
  display: flex;
  align-items: center;
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
