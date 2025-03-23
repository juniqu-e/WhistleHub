# custom_formatter.py
import logging
import re


class PlainFormatter(logging.Formatter):
    def format(self, record):
        # 기본 포맷 처리
        message = super().format(record)
        # ANSI 색상 코드 제거 (정규표현식 사용)
        ansi_escape = re.compile(r'\x1B\[[0-?]*[ -/]*[@-~]')
        return ansi_escape.sub('', message)


class CustomColorFormatter(logging.Formatter):
    # ANSI 색상 코드 매핑 (필요에 따라 조정)
    COLORS = {
        'DEBUG': "\033[36m",    # Cyan
        'INFO': "\033[32m",     # Green
        'WARNING': "\033[33m",  # Yellow
        'ERROR': "\033[31m",    # Red
        'CRITICAL': "\033[41m",  # Red background
    }
    TIME_COLOR = "\033[35m"  # Blue
    THREAD_COLOR = "\033[96m"  # Cyan
    RESET = "\033[0m"

    def format(self, record):
        level = record.levelname
        # ANSI 색상 적용 (여분의 공백 없이)
        color = self.COLORS.get(level, "")
        record.levelname = f"{color}{level}{self.RESET}"
        record.thread = f"{self.THREAD_COLOR}{record.thread}{self.RESET}"
        return super().format(record)

    def formatTime(self, record, datefmt=None):
        # 기본 시간 문자열 생성 후 ANSI 컬러 적용
        time_str = super().formatTime(record, datefmt)
        return f"{self.TIME_COLOR}{time_str}{self.RESET}"
