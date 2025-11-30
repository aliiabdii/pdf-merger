@param gg.jte.Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="author" content="Ali Abdi">
    <meta name="description" content="A simple PDF merging website">
    <meta name="keywords" content="PDF, merge, combine, files">
    <title>PDF Merger</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
        }
        .sidebar {
            width: 200px;
            background-color: #f4f4f4;
            padding: 20px;
            box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
            min-height: 100vh;
            position: sticky;
            top: 0;
            overflow: auto;
        }
        .sidebar img {
            max-width: 100%;
            margin-bottom: 20px;
        }
        .sidebar nav a {
            display: block;
            color: #333;
            text-decoration: none;
            margin: 10px 0;
            padding: 10px;
            border-radius: 5px;
        }
        .sidebar nav a:hover {
            background-color: #ddd;
        }
        .content {
            flex: 1;
            padding: 20px;
        }
        h1 {
            color: #444;
        }

        .menu-toggle {
            display: none;
            background-color: #333;
            color: white;
            padding: 10px 15px;
            border: none;
            cursor: pointer;
            width: 100%;
            text-align: left;
        }

        @media (max-width: 768px) {
            body {
                display: block;
            }
            .menu-toggle {
                display: block;
            }
            .sidebar {
                width: 100%;
                min-height: auto;
                position: static;
                padding: 0;
                transform: translateX(-100%);
                max-height: 0;
                overflow: hidden;
                transition: max-height 0.3s ease-out;
            }
            .sidebar.is-active {
                max-height: 500px;
                transform: translateX(0);
            }
            .sidebar nav {
                padding: 10px 20px 20px;
            }
        }

        /* CSS for the loading spinner (minimal styling) */
        .loading::after {
            content: " ↻";
            display: inline-block;
            animation: spin 1s linear infinite;
        }
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
        .error-message {
            color: red;
            margin-top: 10px;
        }
    </style>
</head>
<body>
<button class="menu-toggle" aria-label="Open Menu" onclick="toggleMenu()">☰ Menu</button>
<div class="sidebar" id="sidebar">
    <img src="logo.png" alt="Logo">
    <nav>
        <a href="/">Merge</a>
        <a href="/about">About</a>
    </nav>
</div>
<div class="content">
    <section>
        ${content}
    </section>
</div>
<script>
    function toggleMenu() {
        const sidebar = document.getElementById('sidebar');
        sidebar.classList.toggle('is-active');
    }
</script>
</body>
</html>