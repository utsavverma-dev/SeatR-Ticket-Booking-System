import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const CreateEvent = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    venueId: '',
    eventDate: '',
    eventTime: '',
    status: 'UPCOMING',
    prices: [
      { category: 'PREMIUM', price: 100 },
      { category: 'STANDARD', price: 50 }
    ]
  });
  
  const [venues, setVenues] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchVenues = async () => {
      try {
        const res = await api.get('/venues');
        setVenues(res.data.data || []);
      } catch (err) {
        console.error("Failed to fetch venues", err);
      }
    };
    fetchVenues();
  }, []);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handlePriceChange = (index, value) => {
    const newPrices = [...formData.prices];
    newPrices[index].price = parseFloat(value) || 0;
    setFormData({ ...formData, prices: newPrices });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    try {
      await api.post('/events', formData);
      setSuccess('Event created successfully! Redirecting...');
      setTimeout(() => navigate('/dashboard'), 2000);
    } catch (err) {
      if (err.response?.data?.errors) {
        const errorMessages = Object.values(err.response.data.errors).flat().join(', ');
        setError(errorMessages);
      } else {
        setError(err.response?.data?.message || 'Failed to create event');
      }
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-in pb-24">
      <div className="text-center mb-10">
        <h1 className="text-3xl md:text-4xl font-extrabold text-stone-100 tracking-tight font-[Montserrat]">Create New Event</h1>
        <p className="text-stone-400 mt-2 text-lg">Set up a new movie, concert, or show.</p>
      </div>
      
      {error && (
        <div className="bg-rose-900/20 border border-rose-900/50 text-rose-400 p-4 rounded-xl mb-8 shadow-sm flex items-start gap-3">
          <svg className="w-5 h-5 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
          <p className="font-medium">{error}</p>
        </div>
      )}
      
      {success && (
        <div className="bg-emerald-900/20 border border-emerald-900/50 text-emerald-400 p-4 rounded-xl mb-8 shadow-sm flex items-start gap-3">
          <svg className="w-5 h-5 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>
          <p className="font-medium">{success}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="glass-card p-8 md:p-10 rounded-3xl border border-stone-800 space-y-8">
        
        {/* Basic Details */}
        <div className="space-y-6">
          <h2 className="text-xl font-bold text-stone-100 border-b border-stone-800 pb-4 font-[Montserrat]">Basic Details</h2>
          
          <div>
            <label className="block text-sm font-semibold text-stone-300 mb-2">Event Title</label>
            <input type="text" name="title" required value={formData.title} onChange={handleChange} className="input-field" placeholder="e.g. Taylor Swift - The Eras Tour" />
          </div>
          
          <div>
            <label className="block text-sm font-semibold text-stone-300 mb-2">Description</label>
            <textarea name="description" rows="4" required value={formData.description} onChange={handleChange} className="input-field resize-none" placeholder="Describe the event..."></textarea>
          </div>
        </div>

        {/* When and Where */}
        <div className="space-y-6 pt-6">
          <h2 className="text-xl font-bold text-stone-100 border-b border-stone-800 pb-4 font-[Montserrat]">When and Where</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold text-stone-300 mb-2">Venue</label>
              <select name="venueId" required value={formData.venueId} onChange={handleChange} className="input-field appearance-none cursor-pointer">
                <option value="" disabled className="text-stone-500">Select a venue</option>
                {venues.map(v => (
                  <option key={v.id} value={v.id} className="text-stone-900">{v.name} ({v.city})</option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold text-stone-300 mb-2">Date</label>
                <input type="date" name="eventDate" required value={formData.eventDate} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-semibold text-stone-300 mb-2">Time</label>
                <input type="time" name="eventTime" required value={formData.eventTime} onChange={handleChange} className="input-field" />
              </div>
            </div>
          </div>
        </div>

        {/* Pricing */}
        <div className="space-y-6 pt-6">
          <h2 className="text-xl font-bold text-stone-100 border-b border-stone-800 pb-4 font-[Montserrat]">Pricing</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-stone-900/50 p-6 rounded-2xl border border-stone-800">
              <label className="block text-sm font-semibold text-stone-300 mb-2">Standard Ticket Price</label>
              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-500 font-bold">$</span>
                <input type="number" required min="0" step="0.01" value={formData.prices[1]?.price || ''} onChange={(e) => handlePriceChange(1, e.target.value)} className="input-field pl-10 font-mono text-lg text-amber-500" placeholder="0.00" />
              </div>
            </div>
            
            <div className="bg-stone-900/50 p-6 rounded-2xl border border-stone-800">
              <label className="block text-sm font-semibold text-stone-300 mb-2">Premium Ticket Price</label>
              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-500 font-bold">$</span>
                <input type="number" required min="0" step="0.01" value={formData.prices[0]?.price || ''} onChange={(e) => handlePriceChange(0, e.target.value)} className="input-field pl-10 font-mono text-lg text-amber-500" placeholder="0.00" />
              </div>
            </div>
          </div>
        </div>

        <button type="submit" className="w-full btn-glow py-4 text-lg mt-8">
          Create Event
        </button>
      </form>
    </div>
  );
};

export default CreateEvent;
