import { hotels } from '../data'
export function recommendHotels({ location='', budget='', amenities=[] }) { return hotels.map(h=>({ ...h, score:(location && h.location.toLowerCase().includes(location.toLowerCase())?3:0)+(budget && h.price<=Number(budget)?2:0)+amenities.filter(a=>h.amenities.includes(a)).length })).filter(h=>h.score>0).sort((a,b)=>b.score-a.score).slice(0,3) }
