import api from './api';

export const dashboardService = {
  getRevenuePerEvent: async () => {
    const response = await api.get('/dashboard/revenue');
    return response.data;
  },
  
  getBookingStatsPerEvent: async () => {
    const response = await api.get('/dashboard/bookings');
    return response.data;
  },
  
  getOverallStatistics: async () => {
    const response = await api.get('/dashboard/statistics');
    return response.data;
  },

  getUpcomingEvents: async () => {
    const response = await api.get('/dashboard/upcoming-events');
    return response.data;
  },

  getPopularEvents: async () => {
    const response = await api.get('/dashboard/popular-events');
    return response.data;
  }
};
