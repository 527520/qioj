<template>
  <div id="manageQuestionView">
    <h2>题目管理</h2>
    <a-table
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :scroll="scroll"
      :pagination="{
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total: total,
        showTotal: true,
      }"
      @page-change="onPageChange"
    >
      <template #createTime="{ record }">
        {{ formatTime(record.createTime) }}
      </template>
      <template #judgeConfig="{ record }">
        {{ JSON.stringify(record.judgeConfig) }}
      </template>
      <template #status="{ record }">
        {{ formatStatus(record.status) }}
      </template>
      <template #optional="{ record }">
        <a-space>
          <a-button
            :disabled="record.status === '0'"
            type="primary"
            @click="doUpdate(record)"
            >修改</a-button
          >
          <a-button status="danger" @click="doDelete(record)">删除</a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Question,
  QuestionControllerService,
  QuestionVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

const tableRef = ref();
const dataList = ref([] as QuestionVO);
const total = ref(0);
const searchParams = ref({
  pageSize: 7,
  current: 1,
  sortField: "createTime",
  sortOrder: "descend",
});
const loadData = async () => {
  const res = await QuestionControllerService.listMyQuestionVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，错误信息：" + res.message);
  }
};
/**
 * 监听searchParams变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});
/**
 * 页面加载时请求数据
 */
onMounted(() => {
  loadData();
});

const scroll = {
  x: 1700,
};

const columns = [
  {
    title: "id",
    dataIndex: "id",
    width: 190,
  },
  {
    title: "标题",
    dataIndex: "title",
    ellipsis: true,
    tooltip: true,
    width: 100,
  },
  {
    title: "内容",
    dataIndex: "content",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "标签",
    dataIndex: "tags",
    ellipsis: true,
    tooltip: true,
    width: 150,
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
    width: 100,
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
    width: 100,
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
    width: 190,
  },
  {
    title: "判题用例",
    dataIndex: "judgeCase",
    ellipsis: true,
    tooltip: true,
    width: 150,
  },
  {
    title: "判题配置",
    dataIndex: "judgeConfig",
    ellipsis: true,
    tooltip: true,
    width: 250,
    slotName: "judgeConfig",
  },
  {
    title: "状态",
    dataIndex: "status",
    ellipsis: true,
    tooltip: true,
    width: 100,
    slotName: "status",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const router = useRouter();
const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};

const doDelete = async (question: Question) => {
  const res = await QuestionControllerService.deleteQuestionUsingPost({
    id: question.id,
  });
  if (res.code === 0) {
    message.success("删除成功");
    await loadData();
  } else {
    message.error("删除失败，错误信息：" + res.message);
  }
};

const formatStatus = (status: string) => {
  if (status === "0") {
    return "答案验证中";
  } else if (status === "1") {
    return "草稿";
  } else if (status === "2") {
    return "创建失败";
  }
  return "已创建";
};

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate() - 1).padStart(2, "0");
  const hours = String(date.getUTCHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};
</script>

<style scoped>
#manageQuestionView {
}
</style>
