<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="holoModal"
     class="fixed inset-0 z-[9999] hidden flex items-center justify-center bg-black/80 backdrop-blur-sm transition-all duration-500 ease-[cubic-bezier(0.25,0.8,0.25,1)] opacity-0 pointer-events-none"
     onclick="closeHoloModal(event)">

  <div id="holoCardContainer" class="relative transform transition-all duration-500 ease-[cubic-bezier(0.34,1.56,0.64,1)] scale-50 opacity-0 translate-y-10">

    <button onclick="closeHoloModal(event)" class="absolute -top-16 right-0 md:-right-16 text-white/70 hover:text-white transition-colors p-2 z-50">
      <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
    </button>

    <div class="card" id="holoCard">
      <div class="card__translater">
        <div class="card__rotator">
          <img id="holoCardImg" class="card__front" src="" alt="Holo Card" />
          <div class="card__shine"></div>
          <div class="card__glare"></div>
        </div>
      </div>
    </div>

    <p class="text-white/40 text-center mt-12 text-[10px] font-medium tracking-[0.2em] uppercase animate-pulse select-none">
      Move cursor to interact
    </p>
  </div>
</div>

<style>
  :root {
    --mx: 50%; --my: 50%; --s: 1; --o: 0;
    --rx: 0deg; --ry: 0deg; --posx: 50%; --posy: 50%; --hyp: 0;
  }

  .card {
    width: 320px;
    height: 446px;
    position: relative;
    z-index: 10;
    touch-action: none;
    perspective: 600px;
  }

  .card__translater,
  .card__rotator {
    width: 100%; height: 100%;
    border-radius: 20px;
    position: relative;
    transform-style: preserve-3d;
  }

  .card__rotator {
    transform: rotateY(var(--ry)) rotateX(var(--rx));
    transition: transform 0.1s ease-out;
    box-shadow:
            0 0 3px -1px rgba(255,255,255,0.4) inset,
            0 50px 100px -20px rgba(0,0,0,0.6);
  }

  .card__front {
    width: 100%; height: 100%;
    border-radius: 20px;
    object-fit: cover;
    background-color: #1a1a1a;
    /* 이미지 선명도 유지 */
    image-rendering: -webkit-optimize-contrast;
  }

  /* =========================================
     Holographic Shine (수정됨: 색감 보존)
     ========================================= */
  .card__shine {
    display: grid;
    position: absolute;
    top: 0; left: 0; bottom: 0; right: 0;
    border-radius: 20px;
    z-index: 3;
    transform: translateZ(1px);
    overflow: hidden;
    background: transparent;
    /* [중요] color-dodge 대신 overlay나 soft-light 사용으로 색감 왜곡 방지 */
    mix-blend-mode: overlay;
    /* [중요] 필터 제거하여 원본 색상 유지 */
    opacity: var(--o);
    pointer-events: none;
  }

  .card__shine:before,
  .card__shine:after {
    content: "";
    grid-area: 1/1;
    transform: translateZ(1px);
    border-radius: 20px;
  }

  /* 1차 패턴: 무지개 (투명도 대폭 조절) */
  .card__shine:before {
    background-image: linear-gradient(
            115deg,
            transparent 25%,
            rgba(0, 231, 255, 0.4) 45%, /* 투명도 0.4로 낮춤 */
            rgba(255, 0, 231, 0.4) 55%, /* 투명도 0.4로 낮춤 */
            transparent 75%
    );
    background-position: var(--posx) var(--posy);
    background-size: 300% 300%;
    mix-blend-mode: screen; /* 부드럽게 얹기 */
    z-index: 2;
  }

  /* 2차 패턴: 텍스처 (은은하게) */
  .card__shine:after {
    background-position: 50% 50%;
    background-size: 200% 200%;
    background-blend-mode: soft-light; /* 강한 대비 제거 */
    z-index: 1;
    opacity: 0.6; /* 텍스처 강도 조절 */

    background-position:
            calc(50% + (50% - var(--posx)) * 2.5) calc(50% + (50% - var(--posy)) * 2.5),
            calc(50% + (50% - var(--posx)) * 1.5) calc(50% + (50% - var(--posy)) * 1.5),
            var(--posx) var(--posy);
  }

  /* =========================================
     Glare (반사광)
     ========================================= */
  .card__glare {
    position: absolute;
    top: 0; left: 0; bottom: 0; right: 0;
    border-radius: 20px;
    z-index: 4;
    transform: translateZ(1px);
    background: radial-gradient(
            farthest-corner circle at var(--mx) var(--my),
            rgba(255, 255, 255, 0.7) 0%, /* 하이라이트 강도 조절 */
            rgba(255, 255, 255, 0.1) 30%,
            transparent 90%
    );
    mix-blend-mode: soft-light; /* 부드러운 빛 반사 */
    opacity: var(--o);
    pointer-events: none;
  }
</style>

<script>
  const holoModal = document.getElementById('holoModal');
  const holoContainer = document.getElementById('holoCardContainer');
  const holoCard = document.getElementById('holoCard');
  const holoImg = document.getElementById('holoCardImg');

  window.openHoloModal = function(imgSrc) {
    holoImg.src = imgSrc;

    holoModal.classList.remove('hidden');
    holoModal.classList.remove('pointer-events-none');

    requestAnimationFrame(() => {
      holoModal.classList.remove('opacity-0');
      holoContainer.classList.remove('scale-50', 'opacity-0', 'translate-y-10');
      holoContainer.classList.add('scale-100', 'opacity-100', 'translate-y-0');
    });

    document.addEventListener('mousemove', handleHoloMove);
    document.addEventListener('mouseleave', handleHoloLeave);
    document.addEventListener('touchmove', handleHoloTouch, { passive: false });
    document.addEventListener('touchend', handleHoloLeave);
  }

  window.closeHoloModal = function(e) {
    if (e && e.target.closest('#holoCard') && !e.target.closest('button')) return;

    holoContainer.classList.remove('scale-100', 'opacity-100', 'translate-y-0');
    holoContainer.classList.add('scale-50', 'opacity-0', 'translate-y-10');

    holoModal.classList.add('opacity-0');
    holoModal.classList.add('pointer-events-none');

    setTimeout(() => {
      holoModal.classList.add('hidden');
      resetHoloCard();

      document.removeEventListener('mousemove', handleHoloMove);
      document.removeEventListener('mouseleave', handleHoloLeave);
      document.removeEventListener('touchmove', handleHoloTouch);
      document.removeEventListener('touchend', handleHoloLeave);
    }, 400);
  }

  function handleHoloMove(e) {
    if (holoModal.classList.contains('hidden')) return;
    updateHoloEffect(e.clientX, e.clientY);
  }

  function handleHoloTouch(e) {
    e.preventDefault();
    const touch = e.touches[0];
    updateHoloEffect(touch.clientX, touch.clientY);
  }

  function updateHoloEffect(x, y) {
    const rect = holoCard.getBoundingClientRect();

    const relX = x - rect.left;
    const relY = y - rect.top;

    const center = { x: rect.width / 2, y: rect.height / 2 };

    const rotateX = ((relY - center.y) / center.y) * -20;
    const rotateY = ((relX - center.x) / center.x) * 20;

    const hyp = Math.sqrt(Math.pow(rotateX, 2) + Math.pow(rotateY, 2)) / 20;

    holoCard.style.setProperty('--rx', rotateX.toFixed(2) + 'deg');
    holoCard.style.setProperty('--ry', rotateY.toFixed(2) + 'deg');

    holoCard.style.setProperty('--mx', ((relX / rect.width) * 100).toFixed(1) + '%');
    holoCard.style.setProperty('--my', ((relY / rect.height) * 100).toFixed(1) + '%');

    holoCard.style.setProperty('--posx', ((relX / rect.width) * 50 + 25).toFixed(1) + '%');
    holoCard.style.setProperty('--posy', ((relY / rect.height) * 50 + 25).toFixed(1) + '%');

    // 반사광 강도 조절 (너무 번쩍이지 않게)
    holoCard.style.setProperty('--hyp', Math.min(hyp, 0.7).toFixed(2));
    holoCard.style.setProperty('--o', '1');
  }

  function handleHoloLeave() {
    resetHoloCard();
  }

  function resetHoloCard() {
    holoCard.style.setProperty('--rx', '0deg');
    holoCard.style.setProperty('--ry', '0deg');
    holoCard.style.setProperty('--o', '0');

    setTimeout(() => {
      holoCard.style.transition = 'transform 0.5s ease-out';
    }, 100);
  }
</script>