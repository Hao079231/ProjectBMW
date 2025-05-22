<html>
<head>
    <title>Sign Up & Login Page</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Roboto', sans-serif;
        }
        .hidden {
            display: none;
        }
    </style>
</head>
<body class="bg-gray-100 flex flex-col items-center justify-center min-h-screen">
    <!-- Navigation Buttons -->
    <div class="flex space-x-4 mb-6">
        <a href="ProductList" class="bg-orange-500 text-white px-6 py-2 rounded-full">Home</a>
        <button id="show-login" class="bg-orange-500 text-white px-6 py-2 rounded-full">Login</button>
        <button id="show-signup" class="bg-orange-500 text-white px-6 py-2 rounded-full">Sign Up</button>
    </div>

    <div class="bg-white shadow-lg rounded-lg flex max-w-4xl w-full relative">
        <!-- Sign Up Form -->
        <div id="signup-form" class="w-1/2 p-8 hidden">
            <h2 class="text-3xl font-bold mb-4">Sign Up For Client</h2>
            <p class="text-gray-500 text-center mb-4">Use your personal details to create an account</p>
            <form id="signupForm" action="SignInUp" method="POST">
                <input type="hidden" name="action" value="signup">
                <input type="text" name="username" placeholder="User" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <input type="password" name="password" placeholder="Password" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <input type="text" name="email" placeholder="Email" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <input type="text" name="address" placeholder="Address" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <input type="text" name="phone" placeholder="Phone" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <button type="submit" class="w-full bg-orange-500 text-white py-2 rounded">SIGN UP</button>
            </form>
        </div>

        <!-- Sign In Form -->
        <div id="login-form" class="w-1/2 p-8">
            <h2 class="text-3xl font-bold mb-4">Sign In</h2>
            <form id="loginForm" action="SignInUp" method="POST">
                <input type="hidden" name="action" value="signin">
                <input type="text" name="username" placeholder="User" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <input type="password" name="password" placeholder="Password" class="w-full p-2 mb-4 border border-gray-300 rounded">
                <button type="submit" class="w-full bg-orange-500 text-white py-2 rounded">SIGN IN</button>
            </form>
        </div>

        <!-- Right Side Content -->
        <div class="w-1/2 bg-gradient-to-r from-pink-500 to-red-500 text-white p-8 flex flex-col justify-center items-center rounded-r-lg">
            <h2 class="text-3xl font-bold mb-4">Hello, Friend!</h2>
            <p class="mb-4 text-center">Welcome to our website</p>
        </div>
    </div>

    <script>
        const loginForm = document.getElementById('login-form');
        const signupForm = document.getElementById('signup-form');
        const showLoginButton = document.getElementById('show-login');
        const showSignupButton = document.getElementById('show-signup');
        const loginFormElement = document.getElementById('loginForm');
        const signupFormElement = document.getElementById('signupForm');

        showLoginButton.addEventListener('click', () => {
            loginForm.classList.remove('hidden');
            signupForm.classList.add('hidden');
        });

        showSignupButton.addEventListener('click', () => {
            signupForm.classList.remove('hidden');
            loginForm.classList.add('hidden');
        });

        function addCsrfTokenToForm(form) {
            form.addEventListener('submit', function(event) {
                event.preventDefault();

                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = 'csrfToken';
                const csrfToken = '<%= session.getAttribute("csrfToken") %>';
                csrfInput.value = csrfToken;

                form.appendChild(csrfInput);

                form.submit();
            });
        }

        addCsrfTokenToForm(loginFormElement);
        addCsrfTokenToForm(signupFormElement);
    </script>
</body>
</html>