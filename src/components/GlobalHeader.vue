<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar" @click="goIndex">
            <img class="logo" src="../assets/qiOJ.png" />
            <div class="title">奇 OJ</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <a-dropdown trigger="hover">
        <a-button @click="userLogin" class="user">
          {{ store.state.user?.loginUser?.userName ?? "未登录" }}
        </a-button>
        <template #content>
          <a-doption
            @click="logout"
            v-if="
              (store.state.user?.loginUser?.userRole ??
                ACCESS_ENUM.NOT_LOGIN) !== ACCESS_ENUM.NOT_LOGIN
            "
            >退出登录
          </a-doption>
        </template>
      </a-dropdown>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";
import message from "@arco-design/web-vue/es/message";

const router = useRouter();
const store = useStore();

// 展示在菜单的路由数据组
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item?.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

//路由跳转后，更新选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});
// 默认主页
const selectedKeys = ref(["/"]);

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const userLogin = () => {
  if (
    (store.state.user?.loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN) ===
    ACCESS_ENUM.NOT_LOGIN
  ) {
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    alert(store.state.user?.loginUser?.userName);
  }
};

const goIndex = () => {
  router.push({
    path: "/",
    replace: true,
  });
};

const logout = async () => {
  // 在这里执行退出登录的逻辑，例如清除用户登录状态等
  const res = await UserControllerService.userLogoutUsingPost();
  if (res.code === 0) {
    // 退出登录成功
    await store.dispatch("user/getLoginUser");
    router.push({
      path: "/",
      replace: true,
    });
    console.log("退出登录");
    location.reload();
  } else {
    message.error("登录失败," + res.message);
  }
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.logo {
  height: 48px;
}

.title {
  color: #134cbd;
  margin-left: 6px;
}

.user {
  cursor: pointer;
}

.title-bar {
  cursor: pointer;
}
</style>
