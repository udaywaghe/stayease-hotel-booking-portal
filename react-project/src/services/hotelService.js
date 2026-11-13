import { api } from './api'
import { hotels } from '../data'
export async function getHotels(params={}) {
  try { const { data } = await api.get('/hotels', { params }); return data }
  catch { let result = [...hotels]; const q=(params.location||'').toLowerCase(); if(q) result=result.filter(h=>`${h.location} ${h.name}`.toLowerCase().includes(q)); if(params.maxPrice) result=result.filter(h=>h.price<=Number(params.maxPrice)); if(params.minRating) result=result.filter(h=>h.rating>=Number(params.minRating)); if(params.amenities?.length) result=result.filter(h=>params.amenities.every(a=>h.amenities.includes(a))); return result }
}
export async function getHotel(id) { try { return (await api.get(`/hotels/${id}`)).data } catch { return hotels.find(h=>h.id===id) } }
