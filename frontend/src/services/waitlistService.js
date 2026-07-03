import api from './api';

export const waitlistService = {
  joinWaitlist: async (eventId, seatCategory) => {
    const response = await api.post('/waitlist', { eventId, seatCategory });
    return response.data;
  },
  
  getUserWaitlist: async () => {
    const response = await api.get('/waitlist');
    return response.data;
  },
  
  leaveWaitlist: async (id) => {
    const response = await api.delete(`/waitlist/${id}`);
    return response.data;
  },
  
  acceptOffer: async (token) => {
    const response = await api.post(`/waitlist/offers/${token}/accept`);
    return response.data;
  }
};
