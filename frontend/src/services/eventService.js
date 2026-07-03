import api from './api';

export const getAllEvents = async () => {
  const response = await api.get('/events');
  return response.data;
};

export const getEvent = async (id) => {
  const response = await api.get(`/events/${id}`);
  return response.data;
};
