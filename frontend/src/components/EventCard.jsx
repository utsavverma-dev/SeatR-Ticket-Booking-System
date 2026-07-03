import { Link } from 'react-router-dom';
import { Calendar, MapPin, Clock } from 'lucide-react';

const EventCard = ({ event }) => {
  return (
    <div className="glass-card rounded-2xl overflow-hidden flex flex-col h-full group hover:-translate-y-2 hover:shadow-[0_20px_40px_-15px_rgba(217,119,6,0.3)] transition-all duration-300">
      <div className="h-48 bg-gradient-to-br from-amber-600 to-orange-900 relative overflow-hidden">
        {/* Decorative background circle */}
        <div className="absolute -right-8 -top-8 w-32 h-32 bg-stone-100/10 rounded-full blur-xl group-hover:scale-150 transition-transform duration-700"></div>
        
        <div className="absolute top-4 right-4 bg-stone-950/80 backdrop-blur-md border border-amber-500/20 px-3 py-1 rounded-full text-xs font-bold text-amber-500 uppercase tracking-wider shadow-sm">
          {event.status}
        </div>
        
        {/* Placeholder for event image/icon */}
        <div className="absolute bottom-4 left-4">
          <div className="bg-stone-900/60 backdrop-blur-md p-3 rounded-xl border border-white/10 text-white font-bold text-lg shadow-lg">
            {new Date(event.eventDate).getDate()}
            <div className="text-xs uppercase opacity-80 font-medium text-amber-300">
              {new Date(event.eventDate).toLocaleString('default', { month: 'short' })}
            </div>
          </div>
        </div>
      </div>
      
      <div className="p-6 flex flex-col flex-grow bg-stone-900/40">
        <h3 className="text-xl font-bold text-stone-100 mb-2 line-clamp-1 group-hover:text-amber-500 transition-colors font-[Montserrat]">{event.title}</h3>
        <p className="text-stone-400 text-sm mb-5 line-clamp-2 leading-relaxed">{event.description}</p>
        
        <div className="mt-auto space-y-3 mb-6">
          <div className="flex items-center text-sm text-stone-300 font-medium">
            <MapPin className="w-4 h-4 mr-2 text-amber-500" />
            <span className="truncate">{event.venueName}</span>
          </div>
          <div className="flex items-center text-sm text-stone-300 font-medium">
            <Clock className="w-4 h-4 mr-2 text-amber-500" />
            <span>{event.eventTime}</span>
          </div>
        </div>
        
        <Link 
          to={`/events/${event.id}`} 
          className="btn-ghost w-full"
        >
          View Details
        </Link>
      </div>
    </div>
  );
};

export default EventCard;
