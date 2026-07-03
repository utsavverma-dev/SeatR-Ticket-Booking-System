import { useState, useEffect } from 'react';
import { getAllEvents } from '../services/eventService';
import EventCard from '../components/EventCard';
import { CalendarX } from 'lucide-react';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await getAllEvents();
        if (data.success) {
          setEvents(data.data);
        }
      } catch (err) {
        setError('Failed to load events. Please try again later.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[70vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-amber-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="bg-rose-900/20 text-rose-400 p-8 rounded-2xl text-center border border-rose-900/50 shadow-sm glass-card">
          <h3 className="text-lg font-medium">{error}</h3>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 animate-fade-in">
      <div className="mb-16 text-center max-w-2xl mx-auto">
        <h1 className="text-4xl md:text-5xl font-extrabold text-stone-100 tracking-tight mb-4 font-[Montserrat]">Discover Events</h1>
        <p className="text-lg text-stone-400">Find the best concerts, theater shows, and experiences happening near you.</p>
      </div>

      {events.length === 0 ? (
        <div className="glass-card p-16 rounded-3xl text-center max-w-2xl mx-auto">
          <div className="w-24 h-24 bg-stone-800 rounded-full flex items-center justify-center mx-auto mb-6 border border-stone-700">
            <CalendarX className="w-12 h-12 text-stone-500" />
          </div>
          <h3 className="text-2xl font-bold text-stone-100 mb-3 font-[Montserrat]">No events found</h3>
          <p className="text-stone-400 text-lg">We couldn't find any upcoming events. Please check back later or subscribe to our newsletter.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Events;
