<template>
  <div id="userLogin">
    <h3 style="text-align: center; padding-top: 10px">用户登录</h3>
    <a-form
      :model="form"
      style="max-width: 280px; margin: 0 auto"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" tooltip="手机号/邮箱" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于8位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">登录</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="goRegister">前往注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
// 表单信息
const form = reactive({
  userAccount: "wuqian",
  userPassword: "wqa131452027",
} as UserLoginRequest);

const router = useRouter();
const store = useStore();

// 提交表单，执行登录
const handleSubmit = async () => {
  const res = await UserControllerService.userLoginUsingPost(form);
  // 登陆成功，跳转到主页
  if (res.code === 0) {
    await store.dispatch("user/getLoginUser");
    await router.push({
      path: "/",
      replace: true,
    });
  } else {
    message.error("登录失败," + res.message);
  }
};

const goRegister = () => {
  router.push({
    path: "/user/register",
  });
};
</script>
