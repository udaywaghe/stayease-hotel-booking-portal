import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ArrowRight, Bot, CalendarDays, MapPin, Send, Sparkles, UserRound } from 'lucide-react'
import { PageShell } from '../components/Layout'
import { sendAssistantMessage } from '../services/assistantService'
import { photo, handleHotelImageError } from '../data'

const starter = { role: 'assistant', text: 'Hello! Tell me where you want to go, your nightly budget, or the kind of stay you have in mind. I can search real StayEase hotels, check dates, share sourced attractions and sketch an itinerary.' }
const money = value => Number(value || 0).toLocaleString('en-IN')

function HotelRecommendation({ hotel, meta }) {
  const room = [...(hotel.rooms || [])].filter(item => !meta?.guests || item.capacity >= meta.guests).sort((a, b) => a.price - b.price)[0] || [...(hotel.rooms || [])].sort((a, b) => a.price - b.price)[0]
  const params = new URLSearchParams()
  if (meta?.checkIn) params.set('checkIn', meta.checkIn)
  if (meta?.checkOut) params.set('checkOut', meta.checkOut)
  if (meta?.guests) params.set('guests', meta.guests)
  const search = params.toString()
  const detailsUrl = `/hotels/${hotel.id}${search ? `?${search}` : ''}`
  const bookingParams = new URLSearchParams({ room: room?.name || '' })
  if (meta?.checkIn) bookingParams.set('checkIn', meta.checkIn)
  if (meta?.checkOut) bookingParams.set('checkOut', meta.checkOut)
  if (meta?.guests) bookingParams.set('guests', meta.guests)
  return <article className="assistant-hotel">
    <Link to={detailsUrl} className="assistant-hotel-image"><img src={photo(hotel.image, 650)} alt={hotel.name} onError={handleHotelImageError}/></Link>
    <div className="assistant-hotel-content"><span className="assistant-hotel-location"><MapPin size={13}/>{hotel.location}</span><Link to={detailsUrl}><h3>{hotel.name}</h3></Link><p>{hotel.description}</p><div className="assistant-amenities">{hotel.amenities?.slice(0, 3).map(item => <span key={item}>{item}</span>)}</div><div className="assistant-hotel-actions"><strong>₹{money(hotel.price)} <small>/ night</small></strong><Link className="assistant-details-link" to={detailsUrl}>Details <ArrowRight size={14}/></Link><Link className="assistant-book-link" to={`/booking/${hotel.id}?${bookingParams}`}>Choose stay</Link></div></div>
  </article>
}

function AssistantMessage({ message }) {
  return <div className={`assistant-message ${message.role}`}><span className="assistant-avatar">{message.role === 'assistant' ? <Bot size={16}/> : <UserRound size={16}/>}</span><div className="assistant-bubble"><p>{message.text}</p>
    {message.hotels?.length > 0 && <div className="assistant-recommendations">{message.hotels.map(hotel => <HotelRecommendation key={hotel.id} hotel={hotel} meta={message.meta}/>)}</div>}
    {message.attractions?.length > 0 && <div className="assistant-attractions">{message.attractions.map(item => <a key={item.name} href={item.sourceUrl} target="_blank" rel="noreferrer"><strong>{item.name}</strong><span>{item.description}</span><small>Source: {item.sourceName} <ArrowRight size={12}/></small></a>)}</div>}
    {message.itinerary?.length > 0 && <div className="assistant-itinerary">{message.itinerary.map(day => <section key={day.day}><small>DAY {day.day}</small><strong>{day.title}</strong><span>{day.activities.length ? day.activities.join(' · ') : 'Keep this day flexible and explore at your own pace.'}</span></section>)}</div>}
    {message.estimateNote && <small className="assistant-estimate"><CalendarDays size={13}/>{message.estimateNote}</small>}
  </div></div>
}

export default function Assistant() {
  const [searchParams] = useSearchParams()
  const destination = searchParams.get('destination')
  const [messages, setMessages] = useState([starter])
  const [draft, setDraft] = useState('')
  const [sessionId, setSessionId] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const listRef = useRef(null)
  const inputRef = useRef(null)
  useEffect(() => { listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' }) }, [messages, busy])
  useEffect(() => { if (destination) setDraft(`What can I explore in ${destination}?`) }, [destination])

  async function submit(text = draft) {
    const message = text.trim()
    if (!message || busy) return
    setMessages(current => [...current, { role: 'user', text: message }])
    setDraft(''); setError(''); setBusy(true)
    try {
      const result = await sendAssistantMessage(message, sessionId || undefined)
      setSessionId(result.sessionId)
      setMessages(current => [...current, { role: 'assistant', text: result.reply, hotels: result.hotels, attractions: result.attractions, itinerary: result.itinerary, estimateNote: result.estimateNote, meta: result.search }])
    } catch (err) {
      setError(err.response?.data?.message || 'The assistant could not reach the hotel service. Please retry your message.')
    } finally { setBusy(false); inputRef.current?.focus() }
  }

  return <PageShell><main className="assistant-page"><div className="assistant-heading"><div><span className="eyebrow"><Sparkles size={13}/> YOUR PERSONAL TRAVEL GUIDE</span><h1>Let’s plan <em>somewhere.</em></h1><p>Hotel recommendations come from StayEase inventory. Attraction cards link to official tourism sources.</p></div><span className="assistant-online"><i/> Ready to help</span></div>
    <section className="assistant-panel" aria-label="Travel assistant chat"><div className="assistant-panel-top"><span className="assistant-logo"><Sparkles size={17}/></span><div><strong>StayEase Travel Guide</strong><small>Hotels, ideas and itineraries</small></div><button type="button" onClick={() => { setMessages([starter]); setSessionId(''); setError('') }}>New chat</button></div>
      <div className="assistant-chat" ref={listRef} aria-live="polite">{messages.map((message, index) => <AssistantMessage key={`${index}-${message.role}`} message={message}/>)}{busy && <div className="assistant-thinking"><span/><span/><span/> Finding a thoughtful answer…</div>}</div>
      {error && <div className="assistant-error" role="alert">{error} <button onClick={() => submit(messages.at(-1)?.text)} type="button">Retry</button></div>}
      <div className="assistant-prompts">{[`Find hotels in Mumbai below ${'\u20B9'}5,000`, 'Plan a 3 day Goa trip', 'Hotels with a swimming pool in Delhi'].map(prompt => <button key={prompt} disabled={busy} onClick={() => submit(prompt)}>{prompt}</button>)}</div>
      <form className="assistant-composer" onSubmit={event => { event.preventDefault(); submit() }}><input ref={inputRef} value={draft} onChange={event => setDraft(event.target.value)} placeholder="Tell me what you’re looking for…" aria-label="Message the travel assistant" maxLength={4000}/><button aria-label="Send message" disabled={!draft.trim() || busy}><Send size={17}/></button></form><p className="assistant-footnote">Prices are from current listed room records. Availability is checked only when you provide dates. Nothing is booked until you confirm through the booking flow.</p>
    </section>
  </main></PageShell>
}
