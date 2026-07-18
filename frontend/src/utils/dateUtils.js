/**
 * Formats a date into the format DD.MM.YYYY.
 * @param {string|Date|Array} date - The date to format (can be ISO string, Date object, or [Y, M, D] array)
 * @returns {string} The formatted date or an empty string
 */
export function formatDate(date) {
  if (!date) return ''

  let d;
  if (Array.isArray(date) && date.length === 3) {
    d = new Date(date[0], date[1] - 1, date[2]);
  } else {
    d = new Date(date);
  }

  if (isNaN(d.getTime())) return ''; // Invalid Date

  const day = d.getDate().toString().padStart(2, '0')
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const year = d.getFullYear()

  return `${day}.${month}.${year}`
}

/**
 * Formats a date into the ISO format YYYY-MM-DD.
 * @param {string|Date} date - The date to format
 * @returns {string} The date in format YYYY-MM-DD
 */
export function formatDateISO(date) {
  if (!date) return ''

  const d = new Date(date)
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * Formats a date specifically for backend API requirements (DD-MM-YYYY).
 * @param {string|Date|Array} date - The date to format
 * @returns {string} The date in format DD-MM-YYYY
 */
export function formatDateForAPI(date) {
  if (!date) return ''

  let d;
  if (Array.isArray(date) && date.length === 3) {
    d = new Date(date[0], date[1] - 1, date[2]);
  } else {
    d = new Date(date);
  }

  if (isNaN(d.getTime())) return ''

  const day = d.getDate().toString().padStart(2, '0')
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const year = d.getFullYear()

  // The backend expects the format DD-MM-YYYY for certain endpoints
  return `${day}-${month}-${year}`
}

/**
 * Returns the weekday of a date in German locale.
 * @param {string|Date|Array} date - The date
 * @returns {string} The name of the weekday (e.g., "Montag")
 */
export function formatWeekday(date) {
  if (!date) return ''

  let d;
  if (Array.isArray(date) && date.length === 3) {
    d = new Date(date[0], date[1] - 1, date[2]);
  } else {
    d = new Date(date);
  }

  if (isNaN(d.getTime())) return '';

  const options = { weekday: 'long' }
  return new Intl.DateTimeFormat('de-DE', options).format(d)
}

/**
 * Checks if a date falls on a weekend.
 * @param {string|Date} date - The date to check
 * @returns {boolean} True if Saturday or Sunday
 */
export function isWeekend(date) {
  const d = new Date(date)
  const day = d.getDay()
  return day === 0 || day === 6 // 0 = Sunday, 6 = Saturday
}

/**
 * Calculates a list of working days starting from a start date, skipping weekends.
 * @param {string|Date} startDate - Start date
 * @param {number} days - Number of required working days
 * @returns {Array<Date>} List of date objects
 */
export function getWorkingDays(startDate, days) {
  const result = []
  let currentDate = new Date(startDate)

  while (result.length < days) {
    if (!isWeekend(currentDate)) {
      result.push(new Date(currentDate))
    }
    currentDate.setDate(currentDate.getDate() + 1)
  }

  return result
}

/**
 * Checks if a date is in the past (comparing only the date part).
 * @param {string|Date} date - The date to check
 * @returns {boolean} True if the date is before today
 */
export function isPastDate(date) {
  if (!date) return false

  const selectedDate = new Date(date)
  const today = new Date()

  // Set time to midnight to compare only the date
  today.setHours(0, 0, 0, 0)
  selectedDate.setHours(0, 0, 0, 0)

  return selectedDate < today
}