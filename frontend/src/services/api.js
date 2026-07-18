import axios from 'axios'

/**
 * This file contains all API service objects for interacting with the backend.
 * It configures a central Axios instance (`apiClient`) with a base URL
 * and then exports modular service objects (e.g., `parkingApi`, `calendarApi`)
 * that provide methods for specific API endpoints.
 */

// Base URL for the API, configurable via environment variable VITE_API_BASE_URL.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

/**
 * The configured Axios instance for all API requests.
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * Service for parking-related API calls.
 */
export const parkingApi = {
  /**
   * Retrieves all parking entries.
   * @returns {Promise<Array>} List of all entries
   */
  async getAllEntries() {
    try {
      const response = await apiClient.get('/entries')
      return response.data
    } catch (error) {
      console.error('Error fetching all entries:', error)
      throw error
    }
  },

  /**
   * Retrieves an occupancy overview for the next working days.
   * @returns {Promise<Object>} Overview of parking occupancy
   */
  async getOverview() {
    try {
      const response = await apiClient.get('/overview')
      return response.data
    } catch (error) {
      console.error('Error fetching overview:', error)
      throw error
    }
  },

  /**
   * Creates a new parking entry.
   * @param {Object} entryData - Data of the new entry
   * @returns {Promise<Object>} The created entry
   */
  async createEntry(entryData) {
    try {
      const response = await apiClient.post('/entries', entryData)
      return response.data
    } catch (error) {
      console.error('Error creating entry:', error)
      throw error
    }
  },

  /**
   * Deletes a parking entry by ID.
   * @param {number} id - ID of the entry to delete
   * @returns {Promise<boolean>} True on success
   */
  async deleteEntry(id) {
    try {
      await apiClient.delete(`/entries/${id}`)
      return true
    } catch (error) {
      console.error('Error deleting entry:', error)
      throw error
    }
  },

  /**
   * Updates an existing parking entry.
   * @param {number} id - ID of the entry to update
   * @param {Object} entryData - New data for the entry
   * @returns {Promise<Object>} The updated entry
   */
  async updateEntry(id, entryData) {
    try {
      const response = await apiClient.put(`/entries/${id}`, entryData)
      return response.data
    } catch (error) {
      console.error('Error updating entry:', error)
      throw error
    }
  },

  /**
   * Creates a series of recurring reservations.
   * @param {Object} entryData - Data for the recurring reservation series
   * @param {boolean} preview - Whether to only return a preview of dates (default: false)
   * @returns {Promise<Object>} The result of the reservation process
   */
  async createRecurringReservation(entryData, preview = false) {
    try {
      const response = await apiClient.post('/recurring', entryData, { params: { preview } })
      return response.data
    } catch (error) {
      console.error('Error creating recurring reservation:', error)
      throw error
    }
  },

  /**
   * Deletes an entire recurring reservation series by ID.
   * @param {number} id - ID of the recurring series
   * @returns {Promise<boolean>} True on success
   */
  async deleteRecurringReservation(id) {
    try {
      await apiClient.delete(`/recurring/${id}`)
      return true
    } catch (error) {
      console.error('Error deleting recurring reservation:', error)
      throw error
    }
  },

  /**
   * Updates a recurring reservation series (e.g., name change).
   * @param {number} id - ID of the recurring series
   * @param {Object} entryData - New data for the recurring series
   * @returns {Promise<Object>} The updated reservation series
   */
  async updateRecurringReservation(id, entryData) {
    try {
      const response = await apiClient.put(`/recurring/${id}`, entryData)
      return response.data
    } catch (error) {
      console.error('Error updating recurring reservation:', error)
      throw error
    }
  },
}

/**
 * Service for calendar-related API calls.
 */
export const calendarApi = {
  /**
   * Retrieves dashboard events for a specific period.
   * @param {string} [startDate] - Start date (YYYY-MM-DD, optional)
   * @param {string} [endDate] - End date (YYYY-MM-DD, optional)
   * @returns {Promise<Array>} List of events
   */
  async getEvents(startDate, endDate) {
    try {
      let params = {};
      if (startDate) {
        params.startDate = startDate;
      }
      if (endDate) {
        params.endDate = endDate;
      }
      const response = await apiClient.get('/dashboard-events', { params });
      return response.data;
    } catch (error) {
      console.error('Error fetching calendar events:', error);
      throw error;
    }
  }
}

/**
 * Service for highscore display data.
 */
export const highscoreApi = {
  /**
   * Retrieves highscore data and formats it for the display view.
   * @returns {Promise<Object>} Formatted highscore data
   */
  async getHighscore() {
    try {
      const data = await this.getHighscoreOverview();

      return {
        'Match history': (data.matchHistoryEntries || []).map(item => ({
          label: item.players,
          value: item.type === 'Darts' ? `${item.points} Throws` : item.matchResult,
          secondaryText: item.dateText,
        })),
        'Darts Leaderboard': (data.dartsTop5 || []).map(item => ({
          label: item.name,
          value: item.totalThrows,
          secondaryText: 'Gesamtwürfe',
        })),
        'Kicker leaderboard': (data.kickerTop5 || []).map(item => ({
          label: item.name,
          value: `${item.totalPoints} Wins`,
          secondaryText: 'Gesamtsiege',
        })),
      };
    } catch (error) {
      console.error('Error fetching highscore data for display:', error);
      throw error;
    }
  },

  /**
   * Retrieves raw highscore overview from the backend.
   * @returns {Promise<Object>} Raw highscore overview data
   */
  async getHighscoreOverview() {
    try {
      const response = await apiClient.get('/highscore-overview');
      return response.data;
    } catch (error) {
      console.error('Error fetching highscore overview:', error);
      throw error;
    }
  },
};

/**
 * Service for highscore administrative operations.
 */
export const highscoreAdminApi = {
  /**
   * Retrieves all Darts matches.
   * @returns {Promise<Array>} List of Darts matches
   */
  async getDartsMatches() {
    try {
      const response = await apiClient.get('/matches/darts');
      return response.data;
    } catch (error) {
      console.error('Error fetching Darts matches:', error);
      throw error;
    }
  },

  /**
   * Deletes a specific Darts match.
   * @param {number} id - Match ID
   * @returns {Promise<boolean>} True on success
   */
  async deleteDartsMatch(id) {
    try {
      await apiClient.delete(`/matches/darts/${id}`);
      return true;
    } catch (error) {
      console.error('Error deleting Darts match:', error)
      throw error;
    }
  },

  /**
   * Retrieves all Kicker matches.
   * @returns {Promise<Array>} List of Kicker matches
   */
  async getKickerMatches() {
    try {
      const response = await apiClient.get('/matches/kicker');
      return response.data;
    } catch (error) {
      console.error('Error fetching Kicker matches:', error);
      throw error;
    }
  },

  /**
   * Deletes a specific Kicker match.
   * @param {number} id - Match ID
   * @returns {Promise<boolean>} True on success
   */
  async deleteKickerMatch(id) {
    try {
      await apiClient.delete(`/matches/kicker/${id}`);
      return true;
    } catch (error) {
      console.error('Error deleting Kicker match:', error);
      throw error;
    }
  },

  /**
   * Creates a new Darts entry.
   * @param {Object} entryData - { playerName, totalThrows }
   * @returns {Promise<Object>} The created match
   */
  async createDartsEntry(entryData) {
    try {
      const response = await apiClient.post('/matches/darts', entryData);
      return response.data;
    } catch (error) {
      console.error('Error creating Darts entry:', error);
      throw error;
    }
  },

  /**
   * Creates a new Kicker entry.
   * @param {Object} entryData - { teamAPlayers, teamBPlayers, matchResult }
   * @returns {Promise<Object>} The created match
   */
  async createKickerEntry(entryData) {
    try {
      const response = await apiClient.post('/matches/kicker', entryData);
      return response.data;
    } catch (error) {
      console.error('Error creating Kicker entry:', error);
      throw error;
    }
  },

  /**
   * Updates an existing Darts entry.
   * @param {number} id - The ID of the Darts match to update.
   * @param {Object} entryData - The updated match data (e.g., { playerName, totalThrows }).
   * @returns {Promise<Object>} The updated match data from the server.
   */
  async updateDartsEntry(id, entryData) {
    try {
      const response = await apiClient.put(`/matches/darts/${id}`, entryData);
      return response.data;
    } catch (error) {
      console.error('Error updating Darts entry:', error);
      throw error;
    }
  },

  /**
   * Updates an existing Kicker entry.
   * @param {number} id - The ID of the Kicker match to update.
   * @param {Object} entryData - The updated match data (e.g., { teamAPlayers, teamBPlayers, matchResult }).
   * @returns {Promise<Object>} The updated match data from the server.
   */
  async updateKickerEntry(id, entryData) {
    try {
      const response = await apiClient.put(`/matches/kicker/${id}`, entryData);
      return response.data;
    } catch (error) {
      console.error('Error updating Kicker entry:', error);
      throw error;
    }
  },
};

/**
 * Service for display configuration settings.
 */
export const configApi = {
  /**
   * Retrieves current display configuration.
   * @returns {Promise<Object>} Configuration object
   */
  async getConfig() {
    try {
      const response = await apiClient.get('/config/display');
      return response.data;
    } catch (error) {
      console.error('Error fetching configuration:', error);
      throw error;
    }
  },

  /**
   * Updates display configuration.
   * @param {Object} config - New configuration data
   * @returns {Promise<Object>} The updated configuration
   */
  async updateConfig(config) {
    try {
      const response = await apiClient.post('/config/display', config);
      return response.data;
    } catch (error) {
      console.error('Error updating configuration:', error);
      throw error;
    }
  }
};

export default apiClient
