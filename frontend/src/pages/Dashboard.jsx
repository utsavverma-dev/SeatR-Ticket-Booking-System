import { useState, useEffect } from 'react';
import { dashboardService } from '../services/dashboardService';
import useAuth from '../hooks/useAuth';
import { DollarSign, Ticket, CalendarCheck, XCircle, TrendingUp, CalendarDays, Star } from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();
  const [revenueData, setRevenueData] = useState([]);
  const [bookingStats, setBookingStats] = useState([]);
  const [overallStats, setOverallStats] = useState(null);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [popularEvents, setPopularEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [revRes, statsRes, overallRes, upcomingRes, popularRes] = await Promise.all([
          dashboardService.getRevenuePerEvent(),
          dashboardService.getBookingStatsPerEvent(),
          dashboardService.getOverallStatistics(),
          dashboardService.getUpcomingEvents(),
          dashboardService.getPopularEvents()
        ]);
        
        setRevenueData(revRes.data);
        setBookingStats(statsRes.data);
        setOverallStats(overallRes.data);
        setUpcomingEvents(upcomingRes.data);
        setPopularEvents(popularRes.data);
      } catch (err) {
        setError('Failed to load dashboard data');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
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
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-rose-900/20 text-rose-400 p-6 rounded-2xl border border-rose-900/50 shadow-sm font-medium">{error}</div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 animate-fade-in pb-20">
      <div className="mb-10 flex flex-col md:flex-row justify-between items-start md:items-end gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-stone-100 tracking-tight mb-1 font-[Montserrat]">Dashboard</h1>
          <p className="text-lg text-stone-400 font-medium">Welcome back, {user?.firstName} {user?.lastName}</p>
        </div>
        <span className="bg-amber-600 text-stone-900 text-xs font-bold px-4 py-1.5 rounded-full uppercase tracking-widest shadow-[0_0_15px_rgba(245,158,11,0.4)]">
          {user?.role}
        </span>
      </div>

      {overallStats && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6 mb-12">
          <MetricCard 
            title="Total Revenue" 
            value={`$${revenueData.reduce((acc, curr) => acc + curr.totalRevenue, 0).toLocaleString()}`} 
            icon={<DollarSign className="w-6 h-6 text-emerald-400" />}
            colorClass="bg-emerald-900/30 border-emerald-500/20"
          />
          <MetricCard 
            title="Tickets Sold" 
            value={overallStats.seatsSold} 
            icon={<Ticket className="w-6 h-6 text-amber-500" />}
            colorClass="bg-amber-900/30 border-amber-500/20"
          />
          <MetricCard 
            title="Total Bookings" 
            value={overallStats.totalBookings} 
            icon={<CalendarCheck className="w-6 h-6 text-cyan-400" />}
            colorClass="bg-cyan-900/30 border-cyan-500/20"
          />
          <MetricCard 
            title="Cancellations" 
            value={overallStats.totalCancellations} 
            icon={<XCircle className="w-6 h-6 text-rose-400" />}
            colorClass="bg-rose-900/30 border-rose-500/20"
          />
          <MetricCard 
            title="Avg Occupancy" 
            value={`${overallStats.seatOccupancyPercentage.toFixed(1)}%`} 
            icon={<TrendingUp className="w-6 h-6 text-orange-400" />}
            colorClass="bg-orange-900/30 border-orange-500/20"
          />
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
        <div className="glass-card rounded-3xl overflow-hidden border border-stone-800">
          <div className="p-6 border-b border-stone-700 flex items-center gap-3 bg-stone-900/50">
            <CalendarDays className="w-5 h-5 text-amber-500" />
            <h2 className="text-xl font-bold text-stone-100 font-[Montserrat]">Upcoming Events</h2>
          </div>
          <ul className="divide-y divide-stone-800">
            {upcomingEvents.length === 0 ? (
              <li className="p-8 text-center text-stone-400 font-medium">No upcoming events.</li>
            ) : (
              upcomingEvents.map(ev => (
                <li key={ev.eventId} className="p-6 hover:bg-stone-900/50 transition-colors group cursor-pointer">
                  <p className="font-bold text-stone-200 text-lg group-hover:text-amber-500 transition-colors font-[Montserrat]">{ev.title}</p>
                  <p className="text-sm text-stone-400 mt-1 font-medium">
                    {new Date(ev.eventDateTime).toLocaleDateString()} <span className="mx-2 text-stone-600">•</span> {ev.venueName}
                  </p>
                </li>
              ))
            )}
          </ul>
        </div>
        
        <div className="glass-card rounded-3xl overflow-hidden border border-stone-800">
          <div className="p-6 border-b border-stone-700 flex items-center gap-3 bg-stone-900/50">
            <Star className="w-5 h-5 text-amber-500" />
            <h2 className="text-xl font-bold text-stone-100 font-[Montserrat]">Popular Events</h2>
          </div>
          <ul className="divide-y divide-stone-800">
            {popularEvents.length === 0 ? (
              <li className="p-8 text-center text-stone-400 font-medium">No popular events.</li>
            ) : (
              popularEvents.map(ev => (
                <li key={ev.eventId} className="p-6 hover:bg-stone-900/50 transition-colors flex justify-between items-center group cursor-pointer">
                  <span className="font-bold text-stone-200 text-lg group-hover:text-amber-500 transition-colors font-[Montserrat]">{ev.eventTitle}</span>
                  <span className="bg-amber-900/30 border border-amber-500/30 text-amber-500 text-sm font-bold px-3 py-1 rounded-full shadow-[0_0_10px_rgba(245,158,11,0.2)]">
                    {ev.totalBookings} Bookings
                  </span>
                </li>
              ))
            )}
          </ul>
        </div>
      </div>

      <h2 className="text-2xl font-bold text-stone-100 mb-6 font-[Montserrat]">Performance Breakdown</h2>
      
      <div className="glass-card rounded-3xl overflow-hidden border border-stone-800">
        <div className="overflow-x-auto custom-scrollbar pb-2">
          <table className="w-full text-left border-collapse min-w-[800px]">
            <thead>
              <tr className="bg-stone-900/80 border-b border-stone-700">
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Event Title</th>
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Revenue</th>
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Occupancy</th>
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Tickets Sold</th>
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Bookings</th>
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Waitlist</th>
                <th className="py-5 px-6 font-semibold text-sm text-stone-400 uppercase tracking-wider">Cancellations</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-800">
              {bookingStats.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-12 text-center text-stone-400 font-medium">No events found.</td>
                </tr>
              ) : (
                bookingStats.map((stat) => {
                  const revenue = revenueData.find(r => r.eventId === stat.eventId)?.totalRevenue || 0;
                  return (
                    <tr key={stat.eventId} className="hover:bg-stone-900/30 transition-colors">
                      <td className="py-5 px-6 text-stone-200 font-bold font-[Montserrat]">{stat.eventTitle}</td>
                      <td className="py-5 px-6 text-emerald-400 font-bold glow-accent-text">${revenue.toLocaleString()}</td>
                      <td className="py-5 px-6 text-stone-300">
                        <div className="flex items-center gap-3">
                          <div className="w-20 h-2 bg-stone-800 rounded-full overflow-hidden">
                            <div className="h-full bg-amber-500 rounded-full shadow-[0_0_8px_rgba(245,158,11,0.8)]" style={{ width: `${stat.seatOccupancyPercentage}%` }}></div>
                          </div>
                          <span className="text-sm font-semibold text-stone-200">{stat.seatOccupancyPercentage.toFixed(1)}%</span>
                        </div>
                      </td>
                      <td className="py-5 px-6 text-stone-300 font-medium">{stat.seatsSold}</td>
                      <td className="py-5 px-6 text-stone-300 font-medium">{stat.totalBookings}</td>
                      <td className="py-5 px-6 text-stone-300">
                        {stat.waitlistCount > 0 ? (
                          <span className="bg-orange-900/40 text-orange-400 text-xs font-bold px-3 py-1 rounded-full shadow-[0_0_10px_rgba(249,115,22,0.2)] border border-orange-500/20">{stat.waitlistCount} waiting</span>
                        ) : (
                          <span className="text-stone-500 font-medium">0</span>
                        )}
                      </td>
                      <td className="py-5 px-6 text-stone-300 font-medium">{stat.totalCancellations}</td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

const MetricCard = ({ title, value, icon, colorClass }) => (
  <div className="glass-card p-6 rounded-3xl border border-stone-800 hover:-translate-y-1 hover:border-amber-500/30 transition-all duration-300 group">
    <div className={`w-12 h-12 rounded-2xl flex items-center justify-center mb-4 border ${colorClass} group-hover:scale-110 transition-transform`}>
      {icon}
    </div>
    <h3 className="text-stone-400 font-semibold mb-1 uppercase tracking-wider text-xs">{title}</h3>
    <p className="text-3xl font-extrabold text-stone-100">{value}</p>
  </div>
);

export default Dashboard;
