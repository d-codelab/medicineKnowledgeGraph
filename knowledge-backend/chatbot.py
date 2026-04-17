#!/usr/bin/python
# -*- coding: UTF-8 -*-
"""
@author: juzipi
@file: chatbot.py
@time:2022/07/24
@description:
"""

from question_classify.rule_question_classify import RuleQuestionClassifier
from question_parser.rule_question_parser import RuleQuestionParser
from answer_search.raw_answer_search import RawAnswerSearcher


class ChatBot(object):

    def __init__(self):
        self.classifier = RuleQuestionClassifier()
        self.parser = RuleQuestionParser()
        self.answer_generate = RawAnswerSearcher()
        self.common_answer = "您好，我是您的医药私人助手，希望可以为您解答"

    def answer(self, question):
        question_classify = self.classifier.classify(question)
        # res_classify = self.classifier.classify(sent)

        if not question_classify:
            return [{}, self.common_answer]

        res_sql = self.parser.parser(question_classify)
        final_answers = self.answer_generate.search(res_sql)
        if not final_answers:

            # return self.common_answer

            return [question_classify['args'], self.common_answer]
        else:
            return [question_classify['args'], '\n'.join(final_answers)]


