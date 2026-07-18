import apiClient from './api'

/**
 * Service for managing all Community Lunch data, including events,
 * food catalog, options, and voting.
 */

const API_PATH = '/community-lunches'
const CATALOG_API_PATH = '/food-catalog'

export const lunchService = {
  // --- Lunch Events ---

  /**
   * Fetches upcoming lunch events for the next X days.
   * @param {number} [days=30] - Number of days in the future to fetch events for.
   * @returns {Promise<Array>} A promise that resolves to a list of upcoming lunch events.
   */
  async getUpcomingLunches(days = 30) {
    try {
      const response = await apiClient.get(`${API_PATH}/upcoming`, {
        params: { days },
      })
      return response.data
    } catch (error) {
      console.error('Error fetching upcoming lunch events:', error)
      return []
    }
  },

  /**
   * Fetches lunch events for a specific date range.
   * @param {string} from - Start date in ISO format (YYYY-MM-DD).
   * @param {string} to - End date in ISO format (YYYY-MM-DD).
   * @returns {Promise<Array>} A promise that resolves to a list of lunch events within the range.
   */
  async getCalendarLunches(from, to) {
    try {
      const response = await apiClient.get(API_PATH, {
        params: { from, to },
      })
      return response.data
    } catch (error) {
      console.error('Error fetching calendar lunches:', error)
      return []
    }
  },

  /**
   * Creates a new lunch event.
   * @param {Object} eventData - The event data.
   * @param {string} eventData.date - The date of the event (YYYY-MM-DD).
   * @param {string} eventData.location - The location of the lunch.
   * @param {string} [eventData.note] - An optional note for the event.
   * @param {number[]} [eventData.initialCatalogItemIds] - Optional array of catalog item IDs to add as initial options.
   * @returns {Promise<Object>} A promise that resolves to the newly created event object.
   */
  async createEvent(eventData) {
    const response = await apiClient.post(API_PATH, eventData)
    return response.data
  },

  /**
   * Fetches a single lunch event by its ID.
   * @param {number} eventId - The unique identifier for the lunch event.
   * @returns {Promise<Object>} A promise that resolves to the detailed event object.
   */
  async getEvent(eventId) {
    const response = await apiClient.get(`${API_PATH}/${eventId}`)
    return response.data
  },

  /**
   * Updates the status of a lunch event (e.g., to open or close voting).
   * @param {number} eventId - The ID of the event to update.
   * @param {('DRAFT'|'OPEN'|'CLOSED')} status - The new status for the event.
   * @returns {Promise<Object>} A promise that resolves to the updated event object.
   */
  async updateStatus(eventId, status) {
    const response = await apiClient.patch(`${API_PATH}/${eventId}/status`, null, {
      params: { status },
    })
    return response.data
  },

  /**
   * Deletes a lunch event by its ID.
   * @param {number} eventId - The ID of the event to delete.
   * @returns {Promise<void>}
   */
  async deleteEvent(eventId) {
    await apiClient.delete(`${API_PATH}/${eventId}`)
  },

  /**
   * Removes a food option from a lunch event.
   * @param {number} eventId - The ID of the event.
   * @param {number} optionId - The ID of the option to remove.
   * @returns {Promise<void>}
   */
  async deleteOption(eventId, optionId) {
    await apiClient.delete(`${API_PATH}/${eventId}/options/${optionId}`)
  },

  // --- Food Options ---

  /**
   * Adds a food option from the main catalog to a specific event.
   * @param {number} eventId - The ID of the event to add the option to.
   * @param {number} catalogItemId - The ID of the item from the food catalog.
   * @returns {Promise<Object>} A promise that resolves to the newly added option object.
   */
  async addOptionFromCatalog(eventId, catalogItemId) {
    const response = await apiClient.post(`${API_PATH}/${eventId}/options/from-catalog`, {
      catalogItemId,
    })
    return response.data
  },

  /**
   * Adds a new, custom food option to an event.
   * @param {number} eventId - The ID of the event to add the option to.
   * @param {string} label - The name of the custom food option (e.g., "Custom Pizza").
   * @param {boolean} [saveToDefaultCatalog=false] - If true, saves the custom option to the main food catalog.
   * @returns {Promise<Object>} A promise that resolves to the newly added option object.
   */
  async addCustomOption(eventId, label, saveToDefaultCatalog = false) {
    const response = await apiClient.post(`${API_PATH}/${eventId}/options/custom`, {
      label,
      saveToDefaultCatalog,
    })
    return response.data
  },

  // --- Voting ---

  /**
   * Submits a vote for a food option in a specific lunch event.
   * @param {number} eventId - The ID of the event.
   * @param {number} optionId - The ID of the option being voted for.
   * @param {string} employeeName - The name of the employee who is voting.
   * @returns {Promise<void>}
   */
  async voteForOption(eventId, optionId, employeeName) {
    try {
      await apiClient.put(`${API_PATH}/${eventId}/choices`, {
        optionId,
        employeeName,
      })
    } catch (error) {
      console.error('Error submitting vote:', error)
      throw error
    }
  },

  /**
   * Fetches all votes made by a specific employee across all lunch events.
   * @param {string} employeeName - The name of the employee.
   * @returns {Promise<Array>} A promise that resolves to a list of choices.
   */
  async getMyChoices(employeeName) {
    try {
      const response = await apiClient.get(`${API_PATH}/choices`, {
        params: { employeeName },
      })
      return response.data
    } catch (error) {
      console.error('Error fetching my choices:', error)
      return []
    }
  },

  /**
   * Fetches the aggregated voting results for a specific lunch event.
   * @param {number} eventId - The ID of the event.
   * @returns {Promise<Object|null>} A promise that resolves to the results object, or null on error.
   */
  async getResults(eventId) {
    try {
      const response = await apiClient.get(`${API_PATH}/${eventId}/results`)
      return response.data
    } catch (error) {
      console.error('Error fetching results:', error)
      return null
    }
  },

  // --- Food Catalog Management ---

  /**
   * Fetches all items from the shared food catalog.
   * @param {boolean} [activeOnly=true] - If true, only returns items marked as 'active'.
   * @returns {Promise<Array>} A promise that resolves to a list of catalog items.
   */
  async getCatalogItems(activeOnly = true) {
    try {
      const params = {}
      if (activeOnly === false) {
        params.activeOnly = false
      }
      const response = await apiClient.get(CATALOG_API_PATH, {
        params,
      })
      return response.data
    } catch (error) {
      console.error('Error fetching food catalog:', error)
      return []
    }
  },

  /**
   * Creates a new item in the food catalog.
   * @param {string} label - The name of the food item (e.g., "Pizza Salami").
   * @returns {Promise<Object>} A promise that resolves to the newly created catalog item.
   */
  async createCatalogItem(label) {
    const response = await apiClient.post(CATALOG_API_PATH, { label })
    return response.data
  },

  /**
   * Updates a food catalog item's label.
   * @param {number} id - The ID of the catalog item to update.
   * @param {string} label - The new name for the food item.
   * @returns {Promise<Object>} A promise that resolves to the updated catalog item.
   */
  async updateCatalogItem(id, label) {
    const response = await apiClient.put(`${CATALOG_API_PATH}/${id}`, { label })
    return response.data
  },

  /**
   * Activates or deactivates a food catalog item.
   * @param {number} id - The ID of the catalog item to update.
   * @param {boolean} active - The new active status.
   * @returns {Promise<void>}
   */
  async updateCatalogItemActive(id, active) {
    await apiClient.patch(`${CATALOG_API_PATH}/${id}`, null, {
      params: { active },
    })
  },
}
