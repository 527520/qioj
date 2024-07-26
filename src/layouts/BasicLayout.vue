<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <!--<a href="#" target="_blank">这是吴奇安第一个正式的项目</a>-->
        <a-space align="center" size="large">
          <a href="https://beian.miit.gov.cn/" target="_blank" class="beian"
            >赣ICP备2024039697号-1</a
          >
          <a href="https://github.com/527520" class="github" target="_blank">
            <icon-github class="github-icon" size="15px" />
            GitHUb
          </a>
        </a-space>
      </a-layout-footer>
    </a-layout>
  </div>
  <!--  <router-view />-->
</template>

<style scoped>
#userLayout {
}

#userLayout .header {
  //margin-bottom: 16px; box-shadow: #eee 1px 1px 5px;
}

#userLayout .content {
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 16px;
  padding: 20px;
}

#userLayout .footer {
  background: #f5f5f5;
  padding: 10px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}

#userLayout .footer a {
  text-decoration: none;
}

#userLayout .beian {
  color: #007bff;
  transition: color 0.3s;
}

#userLayout .github {
  color: #000;
}

#userLayout .beian:hover {
  color: #0056b3;
}
</style>
<script>
import GlobalHeader from "@/components/GlobalHeader.vue";

export default {
  components: { GlobalHeader },
};
</script>
