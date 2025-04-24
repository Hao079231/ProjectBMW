let currentSlide = 0;

function navigateSlide(index) {
    const slideshow = document.getElementById("slideshow");
    const slides = slideshow.children;
    const totalSlides = slides.length;

    if (index < 0) {
        currentSlide = totalSlides - 1; // If moving back past the first slide, go to the last slide
    } else if (index >= totalSlides) {
        currentSlide = 0; // If moving forward past the last slide, go to the first slide
    } else {
        currentSlide = index;
    }

    // Calculate the width of each slide
    const slideWidth = slides[0].clientWidth;
    // Move the slide
    slideshow.style.transform = `translateX(-${currentSlide * slideWidth}px)`;
}

// Previous Button
document.getElementById("prev").addEventListener("click", () => {
    navigateSlide(currentSlide - 1);
});

// Next Button
document.getElementById("next").addEventListener("click", () => {
    navigateSlide(currentSlide + 1);
});

// Automatically move to the next slide every 5 seconds
setInterval(() => {
    navigateSlide(currentSlide + 1);
}, 5000);