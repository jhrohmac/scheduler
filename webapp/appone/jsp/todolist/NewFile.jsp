<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Overlay Example</title>
  <link rel="stylesheet" href="your-styles.css"> <!-- Add your own styles if needed -->
  <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
  <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  <script>
    $(function() {
      function runEffect() {
        var selectedEffect = $("#effectTypes").val();
        var options = {};

        if (selectedEffect === "scale") {
          options = { percent: 50 };
        } else if (selectedEffect === "size") {
          options = { to: { width: 200, height: 60 } };
        }

        var buttonPosition = $("#button").offset();
        var overlay = $("<div id='overlay'></div>");
        overlay.css({
          position: "absolute",
          top: buttonPosition.top,
          left: buttonPosition.left + $("#button").outerWidth(),
          width: $("#effect").outerWidth(),
          height: $("#effect").outerHeight(),
          background: "rgba(0, 0, 0, 0.5)",
          zIndex: 1000,
          display: "none"
        });

        // Append the overlay to the body
        overlay.appendTo("body");

        // Run the effect
        $("#effect").toggle(selectedEffect, options, 500);

        // Show/hide the overlay
        overlay.toggle();
      }

      // Set effect from select menu value
      $("#button").on("click", function() {
        runEffect();
      });
    });
  </script>
  <style>
    /* Your styles go here */
    #effect {
      display: none;
    }

    #overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      z-index: 1000;
      display: none;
    }
  </style>
</head>
<body>

  <div id="effect" class="ui-widget-content ui-corner-all">
    <h3 class="ui-widget-header ui-corner-all">Hide</h3>
    <p>
      Etiam libero neque, luctus a, eleifend nec, semper at, lorem. Sed pede. Nulla lorem metus, adipiscing ut, luctus sed, hendrerit vitae, mi.
    </p>
  </div>

  <button id="button">Toggle Effect</button>

</body>
</html>
