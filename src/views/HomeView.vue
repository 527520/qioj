<template>
  <div class="home">
    <MdEditor :value="mdValue" :handle-change="onMdChange" />
    <!--    <img alt="Vue logo" src="../assets/logo.png" />-->
    <CodeEditor :value="codeValue" :handle-change="onCodeChange" />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import CodeEditor from "@/components/CodeEditor.vue"; // @ is an alias to /src
import MdEditor from "@/components/MdEditor.vue";

const mdValue = ref();
const codeValue = ref();
const onMdChange = (v: string) => {
  mdValue.value = v;
};
const onCodeChange = (v: string) => {
  codeValue.value = v;
};
</script>
