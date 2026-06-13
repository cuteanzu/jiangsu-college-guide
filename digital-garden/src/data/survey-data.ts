import surveyRaw from "./survey-data.json";

export interface SurveyQuestion {
  question: string;     // full question text
  answerCount: number;  // number of student responses
  answers: string[];    // all student answers
}

export interface SchoolSurvey {
  name: string;
  city: string;
  url: string;
  totalResponses: number;
  survey: Record<string, SurveyQuestion>;
}

// Keyed by school code (e.g. "nju", "seu")
const SURVEY_MAP: Record<string, SchoolSurvey> = surveyRaw;

/** Ordered list of question short names for display priority */
export const SURVEY_QUESTION_ORDER = [
  "宿舍", "空调", "独立卫浴", "上床下桌", "食堂",
  "门禁", "断电", "限电", "早晚自习", "晨跑",
  "跑步打卡", "交通", "校园网", "洗衣机", "饮水",
  "电瓶车", "共享单车", "快递", "查寝", "图书馆",
  "通宵自习", "期中考试", "校园卡", "军训",
];

export const SURVEY_QUESTION_ICONS: Record<string, string> = {
  "宿舍": "🛏️", "空调": "❄️", "独立卫浴": "🚿", "上床下桌": "🪜", "食堂": "🍽️",
  "门禁": "🚪", "断电": "⚡", "限电": "🔌", "早晚自习": "📖", "晨跑": "🏃",
  "跑步打卡": "📱", "交通": "🚌", "校园网": "🌐", "洗衣机": "🧺", "饮水": "💧",
  "电瓶车": "🛵", "共享单车": "🚲", "快递": "📦", "查寝": "🔍", "图书馆": "📚",
  "通宵自习": "🌙", "期中考试": "📝", "校园卡": "💳", "军训": "🎖️",
};

export function getSchoolSurvey(schoolCode: string): SchoolSurvey | null {
  return SURVEY_MAP[schoolCode] ?? null;
}

export function getSchoolSurveyByName(schoolName: string): SchoolSurvey | null {
  for (const [code, survey] of Object.entries(SURVEY_MAP)) {
    if (survey.name === schoolName) return survey;
  }
  return null;
}

export function getAllSurveyCodes(): string[] {
  return Object.keys(SURVEY_MAP);
}
