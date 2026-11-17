(()=>{
const L=JSON.parse(localStorage.getItem('exs')||'{}'),sv=()=>localStorage.setItem('exs',JSON.stringify(L));
const D={'GPS nahi mila':'No GPS found','✅ Kuch bhi leak nahi hua':'✅ Nothing leaked','🗺️ Map par kholo':'🗺️ Open in Maps','Koi EXIF nahi':'No EXIF found','(kuch nahi mila)':'(nothing found)','Hex par tap = copy':'Tap a hex = copy','Tip: share karne se pehle "Clean Image" dabao, usse saara metadata hat jata hai.':'Tip: tap "Clean Image" before sharing to remove all metadata.'};
function tr(){const w=document.createTreeWalker(document.body,4);let n;while(n=w.nextNode()){if(n.__o===undefined)n.__o=n.data;const t=n.__o.trim();if(D[t])n.data=L.en?n.__o.replace(t,D[t]):n.__o}}
function th(){const M={dark:['#050810','#0d1322','#1e293b','#e2e8f0','#94a3b8'],light:['#f1f5f9','#ffffff','#cbd5e1','#0f172a','#475569'],amoled:['#000','#080808','#1a1a1a','#e2e8f0','#94a3b8']},t=L.th||'dark';['--bg','--card','--bd','--tx','--mu'].forEach((k,i)=>document.documentElement.style.setProperty(k,M[t][i]));document.body.style.backgroundImage=t=='dark'?'':'none';document.querySelector('header').style.background=t=='light'?'#f1f5f9ee':t=='amoled'?'#000':''}
const hd=document.querySelector('header'),gb=document.createElement('button');gb.textContent='⚙️';gb.style.cssText='background:none;border:0;font-size:22px;color:inherit';hd.appendChild(gb);
const ov=document.createElement('div');ov.style.cssText='position:fixed;inset:0;background:#000a;z-index:20;display:none;align-items:center;justify-content:center;padding:20px';
ov.innerHTML='<div class="card" style="width:100%;max-width:420px"><h3 style="margin:0 0 12px">⚙️ Settings</h3><label>Theme</label><select id="sth" class="btn g" style="margin:6px 0 12px"><option value="dark">Dark</option><option value="amoled">AMOLED</option><option value="light">Light</option></select><label>Language</label><select id="slg" class="btn g" style="margin:6px 0 12px"><option value="">Hinglish</option><option value="1">English</option></select><label style="display:block;margin:8px 0"><input type="checkbox" id="sac"> Auto-clean: photo kholte hi clean karke gallery mein save</label><label style="display:block;margin:8px 0"><input type="checkbox" id="sad"> Address lookup (internet chahiye)</label><button class="btn" id="sx" style="margin-top:10px">Done</button></div>';
document.body.appendChild(ov);
const q=s=>ov.querySelector(s);q('#sth').value=L.th||'dark';q('#slg').value=L.en?'1':'';q('#sac').checked=!!L.ac;q('#sad').checked=L.ad!==false;
gb.onclick=()=>ov.style.display='flex';
q('#sx').onclick=()=>{L.th=q('#sth').value;L.en=q('#slg').value?1:0;L.ac=q('#sac').checked;L.ad=q('#sad').checked;sv();th();tr();ov.style.display='none'};
th();
let last='';const R=render;render=function(){R();extra()};
function extra(){const e=S.ex,G=e?gps(e.g):null;
if(G){const b=.01;$('#p-ex').insertAdjacentHTML('afterbegin','<div class="card"><h3 style="margin:0 0 8px">🗺️ Map</h3><iframe style="width:100%;height:240px;border:0;border-radius:12px" src="https://www.openstreetmap.org/export/embed.html?bbox='+[G.lon-b,G.lat-b,G.lon+b,G.lat+b]+'&marker='+G.lat+','+G.lon+'"></iframe><div id="adr" style="color:var(--mu);font-size:13px;margin-top:8px"></div></div>');
if(L.ad!==false)fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat='+G.lat+'&lon='+G.lon).then(r=>r.json()).then(j=>$('#adr').textContent='📍 '+(j.display_name||'')).catch(()=>$('#adr').textContent='Address ke liye internet chahiye')}
tr();if(L.ac&&last!==S.hash){last=S.hash;$('#bc').click()}}
const bb=document.createElement('button');bb.className='btn g';bb.textContent='🗂️ Batch Scan + Clean (kai photos)';
const fi=Object.assign(document.createElement('input'),{type:'file',accept:'image/*',multiple:true,hidden:true}),bo=document.createElement('div');
$('#bd').before(bb);$('#up .card').after(bo);bb.onclick=()=>fi.click();
fi.onchange=async()=>{const fs=[...fi.files];bo.innerHTML='<div class="card" id="bl"></div>';const bl=$('#bl');let g=0;
for(const [i,f] of fs.entries()){const d=document.createElement('div');d.style.margin='6px 0';d.textContent=(i+1)+'/'+fs.length+' '+f.name+' ...';bl.appendChild(d);
try{const buf=await f.arrayBuffer(),ex=parseExif(buf),G=ex?gps(ex.g):null,im=new Image();im.src=URL.createObjectURL(f);await im.decode();
const k=Math.min(1,4096/Math.max(im.naturalWidth,im.naturalHeight)),c=document.createElement('canvas');c.width=im.naturalWidth*k;c.height=im.naturalHeight*k;c.getContext('2d').drawImage(im,0,0,c.width,c.height);
const b=await new Promise(r=>c.toBlob(r,'image/jpeg',.95));dl(b,'clean_'+f.name);if(G)g++;d.innerHTML=(G?'📍 GPS tha, ':'✅ ')+'clean+saved: '+esc(f.name)}catch(x){d.textContent='❌ '+f.name}
await new Promise(r=>setTimeout(r,700))}
bl.insertAdjacentHTML('beforeend','<b>Done: '+fs.length+' photos, '+g+' mein GPS tha. Gallery > EXIF.X folder dekho.</b>');fi.value=''};
})();
