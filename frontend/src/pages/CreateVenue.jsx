import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const CreateVenue = () => {
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    city: '',
    state: '',
    capacity: 100
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    try {
      const premiumCount = Math.floor(formData.capacity * 0.2);
      const standardCount = formData.capacity - premiumCount;
      
      const payload = {
        name: formData.name,
        address: formData.address,
        city: formData.city,
        state: formData.state,
        seats: [
          { category: 'PREMIUM', rowPrefix: 'P', seatCount: premiumCount > 0 ? premiumCount : 1 },
          { category: 'STANDARD', rowPrefix: 'S', seatCount: standardCount > 0 ? standardCount : 1 }
        ]
      };

      await api.post('/venues', payload);
      setSuccess('Venue created successfully! Redirecting...');
      setTimeout(() => navigate('/dashboard'), 2000);
    } catch (err) {
      if (err.response?.data?.errors) {
        const errorMessages = Object.values(err.response.data.errors).flat().join(', ');
        setError(errorMessages);
      } else {
        setError(err.response?.data?.message || 'Failed to create venue');
      }
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-16 animate-fade-in pb-24">
      <div className="text-center mb-10">
        <h1 className="text-3xl md:text-4xl font-extrabold text-stone-100 tracking-tight font-[Montserrat]">Create New Venue</h1>
        <p className="text-stone-400 mt-2 text-lg">Add a new location for your upcoming events.</p>
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
        <div className="space-y-6">
          <div>
            <label className="block text-sm font-semibold text-stone-300 mb-2">Venue Name</label>
            <input type="text" name="name" required value={formData.name} onChange={handleChange} className="input-field" placeholder="e.g. Grand Arena" />
          </div>
          
          <div>
            <label className="block text-sm font-semibold text-stone-300 mb-2">Address</label>
            <input type="text" name="address" required value={formData.address} onChange={handleChange} className="input-field" placeholder="123 Main St" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold text-stone-300 mb-2">City</label>
              <input type="text" name="city" required value={formData.city} onChange={handleChange} className="input-field" placeholder="New York" />
            </div>
            <div>
              <label className="block text-sm font-semibold text-stone-300 mb-2">State</label>
              <input type="text" name="state" required value={formData.state} onChange={handleChange} className="input-field" placeholder="NY" />
            </div>
          </div>

          <div className="pt-4 border-t border-stone-800">
            <label className="block text-sm font-semibold text-stone-300 mb-2">Total Capacity</label>
            <p className="text-sm text-stone-400 mb-3">We will automatically generate seat rows based on this capacity.</p>
            <input type="number" name="capacity" required min="10" max="10000" value={formData.capacity} onChange={handleChange} className="input-field w-full md:w-1/2 font-mono text-lg text-amber-500" />
          </div>
        </div>

        <button type="submit" className="w-full btn-glow py-4 text-lg mt-8">
          Create Venue
        </button>
      </form>
    </div>
  );
};

export default CreateVenue;
