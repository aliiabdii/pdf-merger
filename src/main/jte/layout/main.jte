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
    </style>
</head>
<body>
<div class="sidebar">
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
</body>
</html>