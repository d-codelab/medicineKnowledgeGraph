<template>
  <div class="content">
    <!-- 菜单栏 -->
    <MenuCard ref="MenuCard"></MenuCard>

    <!-- 内容区域 -->
    <div class="main-content">
      <!-- 左侧对话区 -->
      <div class="dialogue">
        <h2 class="title" style="font-size: 16px;">智能问答机器人</h2>
        <!-- 聊天框 -->
        <div class="chat-box">
          <!-- 聊天内容展示区 -->
          <!-- <el-scrollbar ref="chatMessages" wrap-class="chat-messages">

            <ChatMessage v-for="(message, index) in chatMessages" :key="index" :isUserMessage="message.user === '用户'"
              :message="message.message" :timestamp="message.timestamp" />

          </el-scrollbar> -->
          <el-scrollbar  wrap-class="chat-messages"
                         style="height: 65vh; overflow-y: auto; overflow-x: hidden;">
            <ChatMessage
              v-for="(message, index) in chatMessages"
              :key="index"
              :isUserMessage="message.user === '用户'"
              :message="message.message"
              :timestamp="message.timestamp"
              @template-click="handleTemplateClick"
            />
          </el-scrollbar>
          <!-- 信息输入框和发送按钮 -->
          <div class="input-area">
            <el-input v-model="message" placeholder="请输入消息"></el-input>
            <el-button type="primary" @click="sendMessage">发送</el-button>
          </div>
        </div>
      </div>

      <!-- 右侧知识卡片框和信息展示框 -->
      <div class="knowledge-display">
        <!-- 信息展示框 -->
        <el-scrollbar wrap-class="message-display">
          <h2 class="title-r" style="font-size: 16px;">知识图谱</h2>
          <d3queSearch ref="d3queSearch" :userInput="message" @getData="update" />
          <img v-if="showStaticImage" src="@/assets/queInitial.jpg" alt="Static Image"
               style="height: auto; width: 90%;margin-top: 20px;" />
          <d3que v-if="showGraph" :data="data" :names="names" :labels="labels" :linkTypes="linkTypes" />
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchEventSource } from '@microsoft/fetch-event-source';
// import gSearch from '@/components/gSearch.vue'
import d3queSearch from '@/components/d3queSearch.vue'
import MenuCard from '@/components/MenuCard.vue'
import ChatMessage from '@/components/ChatMessage.vue'
import d3que from '@/components/d3que.vue'
export default {
  components: {
    // gSearch,
    d3queSearch,
    MenuCard,
    d3que,
    ChatMessage
  },
  name: 'ZhiNengque',
  data() {
    return {
      baseURL: 'http://localhost:8090/',
      message: '', // 用于存储用户输入的消息
      chatMessages: [],
      templateTable: [],
      knowledgeCard: '医疗知识，为您解惑答疑，让健康更清晰！',
      showStaticImage: true,
      // d3jsonParser()处理 json 后返回的结果
      data: {
        nodes: [],
        links: []
      },
      // names: ['疾病', '所属科室', '症状'],
      // labels: ['Disease', 'Department', 'Symptom'],
      // linkTypes: ['', 'department_is', 'has_symptom'],
      names: ['疾病', '医疗科目', '症状', '诊断检查项目', '食物', '药品', '在售药品'],
      labels: ['Disease', 'Department', 'Symptom', 'Check', 'Food', 'Drug', 'Producer'],
      linkTypes: ['', 'belongs_to', 'has_symptom', 'need_check', 'recommand_eat', 'do_eat', 'drugs_of', 'no_eat', 'recommand_drug', 'acompany_with', 'common_drug'],
      showGraph: false
    }
  },
  created() {
    this.templateTable = [
      { question: '疾病症状', example: '乳腺癌的症状有哪些？' },
      { question: '已知症状找可能疾病', example: '最近老流鼻涕怎么办？' },
      { question: '疾病病因', example: '为什么有的人会失眠？' },
      { question: '疾病的并发症', example: '失眠有哪些并发症？' },
      { question: '疾病需要忌口的食物', example: '失眠的人不要吃啥？' },
      { question: '疾病建议吃什么食物', example: '耳鸣了吃点啥？' },
      { question: '啥病要吃啥药', example: '肝病要吃啥药？' },
      { question: '疾病需要做什么检查', example: '脑膜炎怎么才能查出来？' },
      { question: '治疗周期', example: '感冒要多久才能好？' },
      { question: '治疗方式', example: '高血压要怎么治？' },
      { question: '治愈概率', example: '白血病能治好吗？' },
      { question: '疾病易感人群', example: '什么人容易得高血压？' },
      { question: '疾病描述', example: '急性胃炎有哪些症状？' }
    ];
    this.initializeChatMessages();
  },
  mounted() {
    this.$nextTick(() => {
      const scrollContainer = this.$refs.ChatMessage.scrollContainer;
      if (scrollContainer && scrollContainer.$el) {
        scrollContainer.$el.scrollTop = scrollContainer.$el.scrollHeight;
      }
    });
  },
  watch: {
    // 监听 chatMessages 数组的变化
    chatMessages: {
      deep: true,
      handler() {
        console.log('chatMessages 数组发生变化，正在滚动到底部...');
        this.scrollToBottom();
      }
    }
  },
  methods: {
    initializeChatMessages() {
      const welcomeMessage = {
        user: '机器人',
        message: this.generateWelcomeMessage(),
        timestamp: new Date().toLocaleString()
      };
      this.chatMessages.push(welcomeMessage);
    },
    generateWelcomeMessage() {
      const intro = [
        '欢迎使用智能问答机器人服务！我可以回答医疗知识相关的各种问题。',
        '请随时向我提问，我会尽力为您提供准确的答案。',
        '如果您不知道该问什么，可以参考下面的提问模板：',
        ''
      ];

      const exampleList = this.templateTable.map((template) => {
        const encoded = encodeURIComponent(template.example);
        return `[${template.example}](template:${encoded})`;
      });

      return `${intro.join('\n')}\n${exampleList.join('&nbsp;&nbsp;&nbsp;')}`;
    },
    handleTemplateClick(example) {
      this.sendMessageFromTemplate(example);
    },
    sendMessageFromTemplate(example) {
      this.message = example;
      this.sendMessage();
    },

    sendMessage() {
      const userMessage = {user: '用户', message: this.message, timestamp: new Date().toLocaleString()}
      const that = this
      this.chatMessages.push(userMessage)
      this.message = ''
      this.showGraph = true
      this.showStaticImage = false

      // 获取问答知识图谱
      this.$refs.d3queSearch.query(userMessage.message);
      const ctrl = new AbortController();

      let robotMessage = {
        user: '机器人',
        message: '',
        timestamp: userMessage.timestamp || new Date().toLocaleString() // 初始化时间戳
      };
      that.chatMessages.push(robotMessage);
      fetchEventSource(this.baseURL + 'api/streamGeneration?question=' + encodeURIComponent(userMessage.message), {
        method: 'GET', // 改为GET请求
        headers: {
          'token': sessionStorage.getItem('token'), // 添加Authorization头
        },
        signal: ctrl.signal, // 传递AbortSignal以支持取消
        openWhenHidden: false, // 浏览器标签页隐藏时断开
        onmessage(event) {
          // 处理从服务器收到的消息
          console.log('event',event)
          const partialReply = event.data;

          // 累积消息
          robotMessage.message += partialReply;
        },
        onopen(event) {
          // 连接打开时的处理
          console.log('onopen', event);
        },
        onclose() {
          // 连接关闭时的处理
          console.log('onclose');
          ctrl.abort(); // 尝试通过AbortController来关闭连接
          console.log('onclose robotMessage-->')
          console.log(robotMessage.message)
          // this.chatMessages.push(robotMessage);
        },
        onerror(err) {
          // 连接错误时的处理
          console.error('onerror', err);
          ctrl.abort(); // 尝试通过AbortController来关闭连接
        }
      })
    },

    scrollToBottom() {

      console.log('正在执行 scrollToBottom 方法');
      console.log('scrollContainer:', this.$refs.scrollContainer);
      const scrollContainer = this.$refs.scrollContainer;
      this.$nextTick(() => {

        if (scrollContainer) {
          scrollContainer.scrollTop = scrollContainer.scrollHeight;
        }
      });
    },
    // 最初问答逻辑
    sendToBackend(message) {
      // // 显示知识图谱
      this.showGraph = true

      // 后端接口调用返回问答信息
      getReply(message)
        .then(response => {
          // 后端返回了机器人的回复
          const robotReply = response.data;
          // 将机器人的回复添加到聊天记录中
          const robotMessage = { user: '机器人', message: robotReply, timestamp: new Date().toLocaleString() };
          this.chatMessages.push(robotMessage);
          this.displayMessage(robotMessage.message, 'robot');
          // 调用另一个后端接口来获取知识图谱数据
          // this.getKnowledgeGraphData(userMessage);
        })
        .catch(error => {
          // 处理发送消息失败的情况
          console.error('发送消息给后端失败:', error);
        })

    },
    // 视图更新
    update(json) {
      console.log('update')
      console.log(json)
      this.d3jsonParser(json)
    },
    /*eslint-disable*/
    // 解析json数据，主要负责数据的去重、标准化
    d3jsonParser(json) {
      const nodes = []
      const links = [] // 存放节点和关系
      const nodeSet = [] // 存放去重后nodes的id

      // // 使用vue直接通过require获取本地json，不再需要使用d3.json获取数据
      // d3.json('./../data/records.json', function (error, data) {
      //   if (error) throw error
      //   graph = data
      //   console.log(graph[0].p)
      // })

      for (let item of json) {
        for (let segment of item.p.segments) {
          // 重新更改data格式
          if (nodeSet.indexOf(segment.start.identity) == -1) {
            nodeSet.push(segment.start.identity)
            nodes.push({
              id: segment.start.identity,
              label: segment.start.labels[0],
              properties: segment.start.properties
            })
          }
          if (nodeSet.indexOf(segment.end.identity) == -1) {
            nodeSet.push(segment.end.identity)
            nodes.push({
              id: segment.end.identity,
              label: segment.end.labels[0],
              properties: segment.end.properties
            })
          }
          links.push({
            source: segment.relationship.start,
            target: segment.relationship.end,
            type: segment.relationship.type,
            properties: segment.relationship.properties
          })
        }
      }
      console.log(nodes)
      console.log(links)
      // this.links = links
      // this.nodes = nodes
      this.data = { nodes, links }
    },
  }
}
</script>

<style lang="scss">
.content {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  height: 100vh;
  overflow: auto;
  background: url(../assets/background2.gif);
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  background-attachment: fixed;
}

.main-content {
  flex: 1;
  flex-grow: 1;
  height: 84vh;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-top: 10px;
}

.dialogue {
  flex: 1;
  position: relative;
  margin-left: 10px;
  padding-left: 20px;
  width: 300px;
  height: 84vh;
  overflow-y: auto;
}

.title {
  text-align: center;
  margin-bottom: 10px;
  color: white;
  border-bottom: 1px solid white;
  padding-bottom: 5px;
}

.chat-box {
  //padding: 15px;
  position: relative;
  // height: calc(84vh - 100px);
  height: 75vh;
}

.el-scrollbar__bar.is-horizontal {
  visibility: hidden !important;
}

.el-scrollbar__bar.is-horizontal {
  display: none !important;
  /* 隐藏水平滚动条 */
}

.input-area {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 10px;
  // z-index: 1;
}

.message-input {
  flex: 1;
  padding: 5px;
  border-radius: 3px;
  margin-right: 10px;
}

.send-button {
  padding: 5px 10px;
  background-color: #1f5f9a;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.welcome-message {
  background-color: #f8f8f8;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.template-list {
  margin-top: 10px;
}

.template-example {
  cursor: pointer;
  color: #409EFF;
  margin-right: 10px;
}

.template-example:hover {
  text-decoration: underline;
}

.knowledge-display {
  flex: 1;
  margin-left: 30px;
  margin-right: 20px;
  width: 60%;
  height: 84vh;
}

.title-r {
  text-align: left;
  color: white;
  padding: 5px 10px 10px 10px;
  border-bottom: 1px solid white;
  border-left: 14px solid #409EFF;
  margin: 0px;
}

.message-display {
  border-radius: 5px;
  margin-top: 10px;
  height: 84vh;
  background-color: rgba(206, 206, 206, 0.5);
  padding-bottom: 20px;
}
</style>
