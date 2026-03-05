(function(){
  const ctx = document.getElementById('ctx').value || '';
  const $ = (id) => document.getElementById(id);

  let symbol = '005930';
  let candles = [];
  let maOptions = [5,20,60,120,240];

  function yyyymmdd(d){
    const y=d.getFullYear(); const m=String(d.getMonth()+1).padStart(2,'0'); const dd=String(d.getDate()).padStart(2,'0');
    return `${y}${m}${dd}`;
  }

  async function postForm(url, param){
    const body = new URLSearchParams(param);
    const res = await fetch(ctx + url, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'}, body });
    return res.json();
  }

  function parseCandles(rows){
    return (rows||[]).map(r=>({
      date: r.date,
      open: Number(r.open||0),
      high: Number(r.high||0),
      low: Number(r.low||0),
      close: Number(r.close||0),
      volume: Number(r.volume||0)
    })).filter(c=>c.close>0);
  }

  async function loadChartOptions(){
    try{
      const r = await postForm('/finance/kisItemchartpriceOptionData.do', {chartId:'KIS_ITEMCHART',seriesType:'MA'});
      const data = r.data || [];
      if(data.length){
        maOptions = data.filter(x=>String(x.enabledYn||'Y')==='Y').map(x=>Number(x.seriesPeriod||0)).filter(v=>v>0);
      }
    }catch(e){ console.error(e); }
  }

  async function loadPrice(){
    const r = await postForm('/finance/getCurrentPriceByInquirePrice.do', {in_stockCode:symbol});
    const s = r.singleData || {};
    const price = Number(s.stck_prpr || s.current_price || s.price || 0);
    const diff = Number(s.prdy_vrss || s.price_change || 0);
    const rate = Number(s.prdy_ctrt || s.price_change_pct || 0);
    const up = diff >= 0;
    $('currentPrice').textContent = price ? price.toLocaleString('ko-KR') : '-';
    $('priceChange').textContent = price ? `${up?'+':''}${diff.toLocaleString('ko-KR')} (${up?'+':''}${rate.toFixed(2)}%)` : '-';
    $('priceTime').textContent = (s.stck_cntg_hour || s.timestamp || '').toString();
    $('currentPrice').className = 'price ' + (up?'up':'dn');
    $('priceChange').className = 'change ' + (up?'up':'dn');
  }

  async function loadChart(){
    const today = new Date();
    const from = new Date(); from.setDate(today.getDate()-180);
    const period = $('periodDivCode').value;

    const r = await postForm('/finance/kisItemchartpriceData.do', {
      in_stockCode: symbol,
      in_fromDate: yyyymmdd(from),
      in_toDate: yyyymmdd(today),
      in_periodDivCode: period,
      in_orgAdjPrc: '0'
    });

    candles = parseCandles(r.data);
    renderChart();
  }

  function calcMA(period){
    const out = new Array(candles.length).fill(null);
    for(let i=period-1;i<candles.length;i++){
      let s=0; for(let j=i-period+1;j<=i;j++) s+=candles[j].close;
      out[i]=s/period;
    }
    return out;
  }

  function renderChart(){
    const canvas = $('mainChart');
    const box = canvas.parentElement;
    const dpr = window.devicePixelRatio || 1;
    canvas.width = box.clientWidth*dpr; canvas.height = box.clientHeight*dpr;
    canvas.style.width = box.clientWidth+'px'; canvas.style.height = box.clientHeight+'px';
    const ctx2 = canvas.getContext('2d');
    ctx2.setTransform(dpr,0,0,dpr,0,0);
    const W=box.clientWidth,H=box.clientHeight;

    if(!candles.length){ ctx2.fillStyle='#8da0c0'; ctx2.fillText('데이터 없음', 20, 30); return; }

    const mL=10,mR=70,mT=20,mB=24,cw=W-mL-mR,ch=H-mT-mB;
    let min=Math.min(...candles.map(c=>c.low)), max=Math.max(...candles.map(c=>c.high));
    const maLines = maOptions.map(p=>({p,arr:calcMA(p)}));
    maLines.forEach(m=>m.arr.forEach(v=>{ if(v!=null){min=Math.min(min,v);max=Math.max(max,v);} }));
    const pad=(max-min)*0.08; min-=pad; max+=pad;

    const x=(i)=>mL+((i+0.5)/candles.length)*cw; const y=(p)=>mT+(1-(p-min)/(max-min))*ch;
    const bw=Math.max(2,cw/candles.length*0.6);

    ctx2.strokeStyle='#1b2a46'; ctx2.lineWidth=0.6;
    for(let i=0;i<=5;i++){ const yy=mT+ch*(i/5); ctx2.beginPath(); ctx2.moveTo(mL,yy); ctx2.lineTo(W-mR,yy); ctx2.stroke(); }

    candles.forEach((c,i)=>{
      const up=c.close>=c.open; const col=up?'#ff5252':'#448aff';
      ctx2.strokeStyle='#60708f'; ctx2.beginPath(); ctx2.moveTo(x(i),y(c.high)); ctx2.lineTo(x(i),y(c.low)); ctx2.stroke();
      ctx2.fillStyle=col; const top=y(Math.max(c.open,c.close)); const bot=y(Math.min(c.open,c.close));
      ctx2.fillRect(x(i)-bw/2, top, bw, Math.max(1,bot-top));
    });

    ['#ff6b6b','#ffd93d','#6bcb77','#4d96ff','#b388ff','#ff9f43','#48dbfb'].forEach((color,idx)=>{
      const m = maLines[idx]; if(!m) return;
      ctx2.strokeStyle=color; ctx2.lineWidth=1.4; ctx2.beginPath();
      let started=false;
      m.arr.forEach((v,i)=>{ if(v==null) return; if(!started){ctx2.moveTo(x(i),y(v)); started=true;} else ctx2.lineTo(x(i),y(v)); });
      ctx2.stroke();
    });

    if($('showVolume').checked){
      const volH=Math.min(100,ch*0.25), base=H-mB;
      const maxV=Math.max(...candles.map(c=>c.volume||0),1);
      candles.forEach((c,i)=>{ const h=(c.volume/maxV)*volH; ctx2.fillStyle=(c.close>=c.open)?'#ff525255':'#448aff55'; ctx2.fillRect(x(i)-bw/2, base-h, bw, h); });
    }
  }

  async function reloadAll(){
    symbol = $('symbolInput').value.trim() || '005930';
    $('stockCode').textContent = symbol;
    $('stockName').textContent = symbol;
    await loadChartOptions();
    await Promise.all([loadPrice(), loadChart()]);
  }

  $('btnLoad').addEventListener('click', reloadAll);
  $('symbolInput').addEventListener('keydown', (e)=>{ if(e.key==='Enter') reloadAll(); });
  $('periodDivCode').addEventListener('change', loadChart);
  $('showVolume').addEventListener('change', renderChart);
  window.addEventListener('resize', renderChart);

  reloadAll();
})();
