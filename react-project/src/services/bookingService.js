import { api } from './api'
const STORAGE='stayease.bookings'
const read=()=>JSON.parse(localStorage.getItem(STORAGE)||'[]')
const write=(items)=>localStorage.setItem(STORAGE,JSON.stringify(items))
export async function createBooking(payload) { try { return (await api.post('/bookings',payload)).data } catch (error) { if (error.response) throw error; const booking={...payload,reference:`SE-${Math.random().toString(36).slice(2,8).toUpperCase()}`,status:'Confirmed',createdAt:new Date().toISOString()}; write([...read(),booking]); return booking } }
export async function findBooking(reference,email) { try { return (await api.get(`/bookings/${reference}`,{params:{email}})).data } catch (error) { if (error.response) throw error; return read().find(b=>b.reference.toLowerCase()===reference.toLowerCase()&&b.email.toLowerCase()===email.toLowerCase())||null } }
export async function cancelBooking(reference,email) { try { return (await api.post(`/bookings/${reference}/cancel`,null,{params:{email}})).data } catch (error) { if (error.response) throw error; const items=read(), index=items.findIndex(b=>b.reference.toLowerCase()===reference.toLowerCase()&&b.email.toLowerCase()===email.toLowerCase()); if(index<0) throw new Error('Booking not found'); items[index].status='Cancelled'; write(items); return items[index] } }
