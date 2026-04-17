<template>
  <div class="chat-message" :class="{ 'user-message': isUserMessage, 'bot-message': !isUserMessage }">
    <el-scrollbar ref="scrollContainer" wrap-class="scroll-wrapper">
      <div class="avatar-message-container">
        <div v-if="isUserMessage" class="message-content user-message-content">
          <div class="message user-text">{{ message }}</div>
          <div class="timestamp">{{ timestamp }}</div>
        </div>
        <img v-if="isUserMessage" src="@/assets/user.png" class="avatar user-avatar" />
        <img v-if="!isUserMessage" src="@/assets/robot.png" class="avatar bot-avatar" />
        <div v-if="!isUserMessage" class="message-content bot-message-content">
          <div class="message markdown-body" v-html="renderedBotMessage" @click="onMarkdownClick"></div>
          <div class="timestamp">{{ timestamp }}</div>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true
})

const defaultRender =
  md.renderer.rules.link_open ||
  function (tokens, idx, options, env, self) {
    return self.renderToken(tokens, idx, options)
  }

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const hrefIndex = tokens[idx].attrIndex('href')
  if (hrefIndex >= 0) {
    const href = tokens[idx].attrs[hrefIndex][1] || ''
    if (href.startsWith('template:')) {
      tokens[idx].attrSet('data-template-link', '1')
      tokens[idx].attrSet('data-template-question', href.slice('template:'.length))
      tokens[idx].attrSet('href', '#')
    }
  }
  return defaultRender(tokens, idx, options, env, self)
}

export default {
  props: {
    isUserMessage: Boolean,
    message: {
      type: String,
      default: ''
    },
    timestamp: String
  },
  computed: {
    renderedBotMessage() {
      if (this.isUserMessage) return ''
      const normalized = this.normalizeMarkdown(this.message || '')
      const rawHtml = md.render(normalized)
      return DOMPurify.sanitize(rawHtml)
    }
  },
  methods: {
    normalizeMarkdown(text) {
      let normalized = (text || '').replace(/\r\n/g, '\n').trim()

      // 为常见中文分节标题补换行，例如“一、二、三、...”
      normalized = normalized.replace(/([^\n])([一二三四五六七八九十]+、)/g, '$1\n\n$2')

      // 将粘连的列表项拆到新行
      normalized = normalized.replace(/([。；：）])\s*-\s+/g, '$1\n- ')

      // 流式输出常把 markdown 表格行挤成 "||"，这里拆分成多行
      normalized = normalized.replace(/\|\|/g, '|\n|')

      // 避免数字编号紧贴正文时无法识别为列表
      normalized = normalized.replace(/([^\n])(\d+\.)\s+/g, '$1\n\n$2 ')

      return normalized
    },
    onMarkdownClick(event) {
      const anchor = event.target.closest('a[data-template-link="1"]')
      if (!anchor) return

      event.preventDefault()
      const encoded = anchor.getAttribute('data-template-question') || ''
      if (!encoded) return
      const question = decodeURIComponent(encoded)
      this.$emit('template-click', question)
    }
  }
}
</script>

<style scoped>
.chat-message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
}

.user-message {
  justify-content: flex-end;
}

.scroll-wrapper {
  overflow-y: auto;
}

.bot-message {
  justify-content: flex-start;
}

.avatar-message-container {
  display: flex;
  align-items: flex-start;
  margin-right: 18px;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-right: 5px;
  margin-top: 5px;
}

.bot-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-left: 5px;
  margin-top: 5px;
}

.message-content {
  display: flex;
  flex-direction: column;
}

.user-message-content {
  margin-right: 10px;
}

.bot-message-content {
  margin-left: 10px;
}

.message {
  max-width: 100%;
  padding: 10px;
  border-radius: 10px;
  word-wrap: break-word;
}

.user-message .message {
  background-color: #409eff;
  color: #000;
}

.bot-message .message {
  background-color: #e5e5ea;
  color: #000;
}

.timestamp {
  font-size: 12px;
  color: #dbdbde;
  margin-top: 5px;
}
/* 让 Markdown 在 scoped 样式下正常显示 */
.markdown-body ::v-deep p {
  margin: 0 0 8px;
}
.markdown-body ::v-deep pre {
  background: #f6f8fa;
  border-radius: 6px;
  padding: 10px;
  overflow-x: auto;
  margin: 8px 0;
}
.markdown-body ::v-deep code {
  font-family: Consolas, Monaco, monospace;
}
.markdown-body ::v-deep ul,
.markdown-body ::v-deep ol {
  padding-left: 20px;
  margin: 8px 0;
}
.markdown-body ::v-deep a {
  color: #2b6cb0;
  text-decoration: underline;
  cursor: pointer;
}
</style>
