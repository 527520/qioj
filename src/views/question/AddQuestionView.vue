<template>
  <div id="addQuestionView">
    <h2>创建题目</h2>
    <a-form :model="form">
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题..." />
      </a-form-item>
      <a-form-item field="tags" label="标签">
        <a-input-tag placeholder="请选择标签" allow-clear v-model="form.tags" />
      </a-form-item>
      <a-form-item field="content" tooltip="请输入题目内容" label="题目内容">
        <MdEditor :value="form.content" :handle-change="onContentChange" />
      </a-form-item>
      <a-form-item
        field="language"
        label="该答案使用的编程语言"
        style="min-width: 240px"
      >
        <a-select
          v-model="form.language"
          :style="{ width: '220px' }"
          placeholder="-编程语言-"
        >
          <a-option value="">-编程语言-</a-option>
          <a-option>java</a-option>
          <a-option>c</a-option>
          <a-option>cpp</a-option>
          <!--<a-option>go</a-option>-->
        </a-select>
      </a-form-item>
      <a-form-item field="answer" tooltip="请输入题目答案" label="题目答案">
        <MdEditor :value="form.answer" :handle-change="onAnswerChange" />
      </a-form-item>

      <!--    <a-form-item field="isRead">-->
      <!--      <a-checkbox v-model="form.isRead"> I have read the manual</a-checkbox>-->
      <!--    </a-form-item>-->
      <a-form-item label="判题配置" :content-flex="false" :merge-props="false">
        <a-space direction="vertical" style="min-width: 380px">
          <a-form-item field="judgeConfig.memoryLimit" label="内存限制">
            <a-input-number
              v-model="form.judgeConfig.memoryLimit"
              placeholder="请输入内存限制"
              mode="button"
              size="large"
              min="0"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.stackLimit" label="堆栈限制">
            <a-input-number
              v-model="form.judgeConfig.stackLimit"
              placeholder="请输入堆栈限制"
              mode="button"
              size="large"
              min="0"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.timeLimit" label="时间限制">
            <a-input-number
              v-model="form.judgeConfig.timeLimit"
              placeholder="请输入时间限制"
              mode="button"
              size="large"
              min="0"
            />
          </a-form-item>
        </a-space>
      </a-form-item>
      <a-form-item
        label="测试用例配置"
        :content-flex="false"
        :merge-props="false"
      >
        <a-form-item
          v-for="(judgeCaseItem, index) of form.judgeCase"
          :key="index"
          no-style
        >
          <!-- 可以在上面两行里面调间距 style="margin-left: -198px"-->
          <a-space direction="vertical" style="min-width: 420px">
            <a-form-item
              :field="`form.judgeCase[${index}].input`"
              :label="`输入用例${index}`"
              :key="index"
            >
              <a-input
                v-model="judgeCaseItem.input"
                placeholder="请输入输入用例..."
              />
            </a-form-item>
            <a-form-item
              :field="`form.judgeCase[${index}].output`"
              :label="`输出用例${index}`"
              :key="index"
            >
              <a-input
                v-model="judgeCaseItem.output"
                placeholder="请输入输出用例..."
              />
            </a-form-item>
            <a-button status="danger" @click="handleDelete(index)">
              删除
            </a-button>
          </a-space>
        </a-form-item>
        <div style="margin-top: 32px">
          <a-button type="outline" status="success" @click="handleAdd"
            >增加测试用例
          </a-button>
        </div>
      </a-form-item>
      <div style="margin-top: 16px" />
      <a-form-item>
        <a-space size="large">
          <a-button
            v-if="form.status !== '3' && form.status !== '99'"
            html-type="submit"
            type="outline"
            @click="saveDraft"
            >保存为草稿
          </a-button>
          <a-button html-type="submit" type="primary" @click="doSubmit"
            >提交
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

// 如果页面地址包含update，就是更新页面
const updatePage = route.path.includes("update");

let form = ref({
  status: "",
  language: "java",
  answer:
    "请将改题目示例答案粘贴到此处，粘贴时请全部替换，验证通过后方可创建题目，**注意**：类名一致为 `Main` ，不可包含包名，示例：\n\n" +
    "public class Main {\n\n" +
    "    public static void main(String[] args) {\n" +
    "        int a = Integer.parseInt(args[0]);\n" +
    "        int b = Integer.parseInt(args[1]);\n" +
    "        System.out.println((a + b));\n\n" +
    "    }\n" +
    "}",
  content: "",
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
  tags: [],
  title: "",
});

/**
 * 根据题目id获取数据
 */
const loadData = async () => {
  const id = route.query.id;
  if (!id) {
    return;
  }
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    id as any
  );
  if (res.code === 0) {
    form.value = res.data as any;
    form.value.language = "";
    // JSON转js对象
    if (!form.value.judgeCase) {
      form.value.judgeCase = [
        {
          input: "",
          output: "",
        },
      ];
    } else {
      form.value.judgeCase = JSON.parse(form.value.judgeCase as any);
    }
    if (!form.value.judgeConfig) {
      form.value.judgeConfig = {
        memoryLimit: 1000,
        stackLimit: 1000,
        timeLimit: 1000,
      };
    } else {
      form.value.judgeConfig = JSON.parse(form.value.judgeConfig as any);
    }
    if (!form.value.tags) {
      form.value.tags = [];
    } else {
      form.value.tags = JSON.parse(form.value.tags as any);
    }
  } else {
    message.error("加载失败!" + res.message);
  }
};
onMounted(() => {
  loadData();
});

router.beforeEach((to, from, next) => {
  if (to.path.includes("add")) {
    resetForm();
  }
  next();
});

const saveDraft = async () => {
  form.value = {
    ...form.value,
    status: "1",
  };
  // 区分更新还是创建
  if (updatePage) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("题目更新成功");
    } else {
      message.error("题目更新失败,错误信息：" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("题目创建成功");
    } else {
      message.error("题目创建失败,错误信息：" + res.message);
    }
  }
};

const doSubmit = async () => {
  if (!form.value.language.trim() || form.value.language.trim() === "") {
    message.error("请选择编程语言");
    return;
  }
  form.value = {
    ...form.value,
    status: "99",
  };
  console.log(form.value);
  // 区分更新还是创建
  if (updatePage) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("题目更新成功");
    } else {
      message.error("题目更新失败,错误信息：" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("题目创建成功");
    } else {
      message.error("题目创建失败,错误信息：" + res.message);
    }
  }
  resetForm();
  setTimeout(() => {
    router.push({
      path: `/my/question`,
    });
  }, 2000);
};

const resetForm = () => {
  form.value = {
    ...form.value,
    language: "java",
    answer:
      "请将改题目示例答案粘贴到此处，粘贴时请全部替换，验证通过后方可创建题目，**注意**：类名一致为 `Main` ，不可包含包名，示例：\n\n" +
      "public class Main {\n\n" +
      "    public static void main(String[] args) {\n" +
      "        int a = Integer.parseInt(args[0]);\n" +
      "        int b = Integer.parseInt(args[1]);\n" +
      "        System.out.println((a + b));\n\n" +
      "    }\n" +
      "}",
    content: "",
    judgeCase: [
      {
        input: "",
        output: "",
      },
    ],
    judgeConfig: {
      memoryLimit: 1000,
      stackLimit: 1000,
      timeLimit: 1000,
    },
    tags: [],
    title: "",
  };
};

/**
 * 新增判题用例
 *
 */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};
/**
 * 删除判题用例
 * @param index
 */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};
const onContentChange = (v: string) => {
  form.value.content = v;
};
const onAnswerChange = (v: string) => {
  form.value.answer = v;
};
</script>

<style scoped>
#addQuestionView {
}
</style>
