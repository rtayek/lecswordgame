from PIL import Image

def create_small_png(filename="small.png", size=(16, 16), color=(255, 0, 0)):
    """
    Creates a small PNG image with the specified size and color.
    """
    img = Image.new('RGB', size, color)
    img.save(filename)
    print(f"Created {filename} with size {size} and color {color}")

if __name__ == "__main__":
    create_small_png("small.png")
