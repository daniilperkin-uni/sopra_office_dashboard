/**
 * Processes raw calendar events into a structured format for the frontend.
 * Groups events by date and enriches them with titles, icons, and colors.
 *
 * @param {Array} events - List of raw event objects from the API.
 * @returns {Object} - A map of date strings (YYYY-MM-DD) to arrays of processed event objects.
 */
export function processCalendarEvents(events) {
  const transformed = {};

  if (!events || !Array.isArray(events)) {
      return transformed;
  }

  events.forEach(event => {
    let personName = event.employeeName || '';
    const shouldAnonymize = import.meta.env.VITE_ANONYMIZE_NAMES === 'true' || (!import.meta.env.PROD && import.meta.env.VITE_ANONYMIZE_NAMES === undefined);
    const displayName = shouldAnonymize ? personName.slice(-10) : personName;

    let title = event.dashboardEventDescription;

    // Logic for title and subtitle using displayName for specific event types
    if (event.dashboardEventType === 'WORK_ANNIVERSARY') {
      const match = event.dashboardEventDescription.match(/(\d+)\s*Jahre/i);
      const years = match ? match[1] : '';
      if (years && displayName) {
        title = `${displayName} ist ${years} Jahre bei itestra!`;
      } else {
        title = event.dashboardEventDescription.replace(/^\d{2}\.\d{2}\.\d{4}\s+/, '');
      }
    } else if (event.dashboardEventType === 'BIRTHDAY') {
       title = `${displayName} hat Geburtstag!`;
    } else if (event.dashboardEventType === 'PROBATION_END') {
       title = `${displayName} Probation Ende`;
    }

    // General cleanup: Remove leading date (DD.MM.YYYY) from the title
    if (title && typeof title === 'string') {
        title = title.replace(/^\d{2}\.\d.2\.\d{4}\s*/, '').trim();
    }

    let label = title; // Set standard label to title for consistency

    // Clean up label date suffix
    if (label && typeof label === 'string') {
        // Remove trailing date: "... am 01.01.2026!" -> "..."
        label = label.replace(/\s+am\s+\d{2}\.\d{2}\.\d{4}.*$/, '!');
        label = label.trim();
    }

    // Add icons for special event types
    if (event.dashboardEventType === 'WORK_ANNIVERSARY') {
      label = `🎉 ${label}`;
    } else if (event.dashboardEventType === 'BIRTHDAY') {
      label = `🎂 ${label}`;
    } else if (event.dashboardEventType === 'PROBATION_END') {
      label = `✅ ${label}`;
    }

    let badge = 'square';
    let color = 'green';
    switch (event.dashboardEventType) {
      case 'BIRTHDAY':
        color = 'green';
        break;
      case 'WORK_ANNIVERSARY':
        color = 'purple';
        break;
      case 'PROBATION_END':
        color = 'red';
        break;
    }

    if (!event.dashboardEventDate) return;

    const dateParts = event.dashboardEventDate.split('.');
    if (dateParts.length < 3) return;

    // Assumes the format DD.MM.YYYY from backend
    const isoDate = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}`;

    if (!transformed[isoDate]) {
      transformed[isoDate] = [];
    }
    transformed[isoDate].push({
      label,
      title,
      badge,
      color,
      ...event
    });
  });
  return transformed;
}
