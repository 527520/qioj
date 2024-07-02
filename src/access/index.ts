import router from "@/router";
import store from "@/store";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";

router.beforeEach(async (to, from, next) => {
  let loginUser = store.state.user.loginUser;
  console.log("登录用户信息：", loginUser);

  // 如果之前没登录过（没有userRole这个属性，这个属性为空），自动登录
  if (!loginUser || !loginUser.userRole) {
    // 加await是为了等用户登录之后在执行后续的代码
    await store.dispatch("user/getLoginUser");
    loginUser = store.state.user.loginUser;
  }

  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  //要跳转的页面必需要登录
  if (needAccess !== "hideInMenu" && needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    //如果没登录，跳转到登录页面
    if (
      !loginUser ||
      !loginUser.userRole ||
      loginUser.userRole === ACCESS_ENUM.NOT_LOGIN
    ) {
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
    if (!checkAccess(loginUser, needAccess)) {
      next("noAuth");
      return;
    }
  }

  next();
});
