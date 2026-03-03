document.addEventListener("DOMContentLoaded", function () {

  const filterBtn = document.getElementById("filterToggleBtn");
  const filterArea = document.getElementById("filterArea");

  filterBtn.addEventListener("click", function () {

    if (filterArea.style.display === "none") {
      filterArea.style.display = "block";
    } else {
      filterArea.style.display = "none";
    }

  });

});
