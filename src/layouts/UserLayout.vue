<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <a-space class="logoBox" @click="goIndex">
          <img src="@/assets/qiOJ.png" class="logo" />
          <div>奇 OJ</div>
        </a-space>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="#" target="_blank">这是吴奇安第一个正式的项目 ————> 吴奇安</a>
      </a-layout-footer>
    </a-layout>
  </div>
  <!--  <router-view />-->
</template>
<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();

const goIndex = () => {
  router.push({
    path: "/",
    replace: true,
  });
};
</script>
<style scoped>
#userLayout {
  //background-size: cover; //background: url("@/assets/background.jpg") no-repeat fixed center center;
}

#userLayout .logo {
  height: 48px;
}

#userLayout .header {
  padding-left: 20px;
  padding-top: 14px;
  padding-bottom: 14px;
  //margin-bottom: 16px; box-shadow: #eee 1px 1px 5px;
}

#userLayout .content {
  margin-bottom: 16px;
}

#userLayout .footer {
  //background: #efefef; padding: 16px; position: sticky; bottom: 0; left: 0; right: 0;
  text-align: center;
}

.logoBox {
  cursor: pointer;
}
</style>
