export function initReviewsSlider() {
  const sliderElement = document.querySelector(".reviews-slider");

  if (sliderElement) {
    new Swiper(".reviews-slider", {
      slidesPerView: 1,
      spaceBetween: 30,
      loop: true,
      observer: true,
      observeParents: true,
      navigation: {
        nextEl: ".swiper-button-next",
        prevEl: ".swiper-button-prev",
      },
      breakpoints: {
        768: { slidesPerView: 2 },
        1024: { slidesPerView: 3 },
      },
    });
  }
}
